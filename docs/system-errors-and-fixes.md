# 🔍 Análise de Erros — Austral Estoque Backend

> **Gerado em:** 2026-05-14  
> **Projeto:** `com.austral.estoque` — Spring Boot 3.3.5 / Java 21  
> **Escopo:** Análise estática de todos os arquivos Java + configuração

---

## Sumário Executivo

| Severidade | Quantidade | Arquivos afetados |
|---|---|---|
| 🔴 **Crítico** (erro de compilação / NullPointer em runtime) | 5 | `JwtTokenProvider`, `AsyncConfig`, `OrderService`, `application.yml` |
| 🟠 **Alto** (lógica incorreta / comportamento imprevisível) | 3 | `JwtTokenProvider`, `OrderService`, `AuthService` |
| 🟡 **Médio** (má prática / degradação de segurança ou performance) | 4 | `Order`, `SecurityConfig`, `ItemController`, `application.yml` |
| 🔵 **Baixo** (code-smell / sugestão de melhoria) | 3 | `AsyncConfig`, `UserDetailsServiceImpl`, `Approval` |

---

## 🔴 ERROS CRÍTICOS

---

### ERRO 1 — `JwtTokenProvider.java` — NPE no construtor + API obsoleta JJWT 0.12.x

**Arquivo:** `backend/src/main/java/com/austral/estoque/security/JwtTokenProvider.java`

#### Problema 1-A: `NullPointerException` no construtor (linhas 34–36)

```java
// ❌ ERRADO — jwtSecret é null no construtor; @Value ainda não foi injetado
public JwtTokenProvider() {
    initKey(); // NPE: jwtSecret.getBytes() → NullPointerException
}
```

O Spring injeta `@Value` **depois** do construtor. Chamar `initKey()` no construtor garante uma `NullPointerException` na inicialização do contexto.

**✅ Correção:**

```java
// Remova a chamada do construtor; o @PostConstruct já é suficiente
public JwtTokenProvider() {
    // construtor vazio — Spring injeta @Value antes de @PostConstruct
}

@PostConstruct
public void initKey() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}
```

---

#### Problema 1-B: API obsoleta/removida do JJWT 0.12.x (múltiplas linhas)

O JJWT 0.12.x (`jjwt.version=0.12.6` no `pom.xml`) **removeu** os métodos legados. Os seguintes métodos **não existem mais** e causarão `NoSuchMethodError`:

| Linha | Código problemático | Motivo |
|---|---|---|
| 67 | `.setSubject(...)` | Removido — use `.subject(...)` |
| 68 | `.setClaims(...)` | Removido — use `.claims(map).add()` ou passe no builder |
| 69 | `.setIssuedAt(...)` | Removido — use `.issuedAt(...)` |
| 70 | `.setExpiration(...)` | Removido — use `.expiration(...)` |
| 71 | `SignatureAlgorithm.HS256` | Classe removida — use `Jwts.SIG.HS256` |
| 71 | `.signWith(key, alg)` | Assinatura mudou — use `.signWith(key, Jwts.SIG.HS256)` |
| 106 | `Jwts.parser().setSigningKey(...)` | Removido — use `Jwts.parser().verifyWith(key).build()` |
| 106 | `.parseClaimsJws(...)` | Removido — use `.parseSignedClaims(...)` |
| 122 | `Jwts.parser().setSigningKey(...)` | Idem |
| 108 | `JwtException` | Não importado — falta `import io.jsonwebtoken.JwtException;` |

Além disso, `SignatureAlgorithm` **não está importado** em nenhuma linha do arquivo, causando erro de compilação.

**✅ Correção completa para `JwtTokenProvider.java`:**

```java
package com.austral.estoque.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey signingKey;

    // ✅ Construtor vazio — @Value é injetado ANTES do @PostConstruct
    public JwtTokenProvider() {}

    @PostConstruct
    public void initKey() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Gera access token */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority()).findFirst().orElse("USER"))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Gera refresh token */
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh_token")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Extrai username do token */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Valida token contra UserDetails */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /** Extrai uma claim do token */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    private boolean isTokenExpired(String token) {
        return getAllClaims(token).getExpiration().before(new Date());
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

> **Atenção:** `JwtAuthFilter` e `AuthService` usam `jwtTokenProvider.extractUsername(jwt)` e `jwtTokenProvider.isTokenValid(jwt, userDetails)`. Esses métodos não existiam na versão antiga — o arquivo corrigido acima já os expõe com a assinatura correta.

---

#### Problema 1-C: `getClaimsFromToken` referencia `e` fora do escopo (linha 126)

```java
// ❌ ERRADO — variável 'e' não existe neste escopo
throw new IllegalArgumentException("Token inválido", e);
```

O `e` é capturado no `catch`, mas o `throw` está fora do bloco `catch`. Isso é **erro de compilação** (`e cannot be resolved to a variable`).

**✅ Correção:** esse método é eliminado pela refatoração acima (substituído por `extractClaim`).

---

### ERRO 2 — `AsyncConfig.java` — `LogFactory.getLog()` inexistente + interface `AsyncUncaughtExceptionHandler` mal implementada

**Arquivo:** `backend/src/main/java/com/austral/estoque/config/AsyncConfig.java`

#### Problema 2-A: `org.springframework.util.LogFactory` não existe (linha 7/26)

```java
import org.springframework.util.LogFactory; // ❌ classe não existe em Spring 6.x
...
LogFactory log = LogFactory.getLog(getClass()); // ❌ erro de compilação
```

`LogFactory` foi **removido** do Spring Framework 6. O projeto usa Slf4j (via Lombok `@Slf4j`).

#### Problema 2-B: `RejectedExecutionHandler` recebe `Runnable`, não tem método `error`

```java
executor.setRejectedExecutionHandler((r, e) -> {
    log.error("Rejeitando tarefa assíncrona", r); // ❌ r é um Runnable, não uma mensagem
});
```

O `log.error(msg, Object)` trata o segundo argumento como throwable ou parâmetro de template. Semanticamente incorreto.

**✅ Correção completa para `AsyncConfig.java`:**

```java
package com.austral.estoque.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean("asyncVirtualThreadExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("austral-async-");
        executor.setRejectedExecutionHandler((runnable, threadPoolExecutor) ->
            log.warn("Tarefa assíncrona rejeitada — pool saturado. Tarefas ativas: {}",
                    threadPoolExecutor.getActiveCount())
        );
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
```

---

### ERRO 3 — `OrderService.java` — Método retorna `Optional` mas lança exceção dentro (linha 32–34)

**Arquivo:** `backend/src/main/java/com/austral/estoque/service/order/OrderService.java`

```java
// ❌ ERRADO — método retorna Optional<Order>, mas o orElseThrow() nunca retorna Optional
public Optional<Order> findByCode(String code) {
    return orderRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Order not found")); // retorna Order, não Optional<Order>
}
```

Isso é um **erro de compilação**: `orElseThrow()` retorna `Order`, não `Optional<Order>`. Além disso, `orderRepository.findByCode(code)` não existe no `OrderRepository` (não foi definido no repositório).

**✅ Correção:**

```java
public Optional<Order> findByCode(String code) {
    return orderRepository.findByCode(code); // retorna Optional diretamente
}
```

E em `OrderRepository` adicionar:

```java
Optional<Order> findByCode(String code);
```

---

### ERRO 4 — `application.yml` — Chave de configuração errada (linha 57)

**Arquivo:** `backend/src/main/resources/application.yml`

```yaml
# ❌ ERRADO — nome de variável de ambiente inconsistente com .env.example
refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_EXPIRATION_MS:604800000}
#                                          ^^^^^^^^^^^^^^^^^^^
# Typo: EXPIRATION duplicado — a variável real no .env.example é JWT_REFRESH_EXPIRATION_MS
```

O `.env.example` define `JWT_REFRESH_EXPIRATION_MS=604800000`, mas o `application.yml` lê `JWT_REFRESH_EXPIRATION_EXPIRATION_MS` (com `EXPIRATION` duplicado). Em produção, o valor nunca será lido da variável de ambiente correta.

**✅ Correção:**

```yaml
refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

---

### ERRO 5 — `application.yml` — Falta o prefixo `spring:` (linha 1)

```yaml
# ❌ ERRADO — arquivo começa sem o prefixo raiz 'spring:'
  application:
    name: austral-estoque
  datasource:
    ...
```

O arquivo inteiro está indentado como se `spring:` existisse, mas a chave raiz está ausente. Isso faz com que o Spring Boot **ignore** todas as propriedades `spring.*` (datasource, jpa, flyway, data, mail, cache).

**✅ Correção:** adicionar `spring:` na linha 1:

```yaml
spring:
  application:
    name: austral-estoque

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/austral_estoque}
    ...
```

---

## 🟠 ERROS DE LÓGICA (ALTO)

---

### ERRO 6 — `AuthService.java` — Seed de admin cria Role sem persistir (linhas 96–108)

**Arquivo:** `backend/src/main/java/com/austral/estoque/service/AuthService.java`

```java
Role adminRole = new Role();
adminRole.setName("ADMIN");
adminRole.setDescription("Administrador do sistema");

User admin = User.builder()
    ...
    .roles(Set.of(adminRole)) // ❌ Role não salvo — sem @Id, JPA falha ou ignora
    .build();

userRepository.save(admin); // Pode lançar TransientPropertyValueException
```

`adminRole` nunca é persistido. Como `User.roles` é `@ManyToMany`, o JPA não persiste a `Role` em cascata. Isso resulta em `org.hibernate.TransientPropertyValueException` ou a role é simplesmente ignorada dependendo da versão do Hibernate.

**✅ Correção:** injetar `RoleRepository` e buscar/salvar a role antes:

```java
private final RoleRepository roleRepository; // adicionar ao construtor

@Override
@Transactional
public void run(ApplicationArguments args) {
    if (!userRepository.existsByEmail(adminEmail)) {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(
                Role.builder().name("ADMIN").description("Administrador do sistema").build()
            ));

        User admin = User.builder()
            .username("admin")
            .email(adminEmail)
            .passwordHash(passwordEncoder.encode(adminPassword))
            .fullName("Administrador")
            .authProvider(User.AuthProvider.LOCAL)
            .active(true)
            .roles(new HashSet<>(Set.of(adminRole)))
            .build();

        userRepository.save(admin);
        log.info("Admin criado com sucesso.");
    }
}
```

> **Nota:** `Role` não tem um builder Lombok — adicionar `@Builder` à classe `Role.java`.

---

### ERRO 7 — `OrderService.java` — Classe com visibilidade package-private (linha 20)

```java
// ❌ ERRADO — sem modificador public
class OrderService {
```

`OrderService` é package-private. O `OrderController` (em subpacote diferente) **não consegue injetar** esse serviço. Spring lança `NoSuchBeanDefinitionException` ou `BeanCreationException`.

**✅ Correção:**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {  // ✅ adicionar public
```

---

### ERRO 8 — `ItemController.java` — Controller acessa repositório diretamente (má arquitetura + bug de serialização)

**Arquivo:** `backend/src/main/java/com/austral/estoque/controller/ItemController.java`

```java
// ❌ Controllers não devem acessar repositórios diretamente
private final ItemRepository itemRepository;
```

Além da violação de arquitetura (Controller → Repository sem Service), a serialização JSON de `Item` expõe a entidade JPA diretamente. Entidades com relacionamentos `LAZY` (`@ManyToOne ItemCategory`) causam `LazyInitializationException` ao serializar fora de uma transação.

**✅ Correção:** criar `ItemService` e usar DTOs de resposta. O Controller deve receber `ItemService`, não `ItemRepository`.

---

## 🟡 ALERTAS DE MÉDIA SEVERIDADE

---

### ALERTA 1 — `Order.java` — Enum com caractere especial (acento) (linha 69)

```java
public enum OrderPaymentMethod {
    DEPÓSITO, DOCUMENTO, // ❌ 'Ó' com acento — pode causar problemas em banco/serialização
    CARTAO_DE_CREDITO, PIX
}
```

Valores de enum com acentos armazenados via `@Enumerated(EnumType.STRING)` podem apresentar inconsistências em bancos de dados com encoding incorreto ou ao comparar strings.

**✅ Correção:**

```java
public enum OrderPaymentMethod {
    DEPOSITO, DOCUMENTO, CARTAO_DE_CREDITO, PIX
}
```

---

### ALERTA 2 — `SecurityConfig.java` — Path de api-docs incorreto (linha 46)

```java
private static final String[] PUBLIC_PATHS = {
    ...
    "/api/v1/api-docs/**"  // ❌ path não corresponde ao springdoc configurado
};
```

No `application.yml`:
```yaml
springdoc:
  api-docs:
    path: /api/v1/api-docs
```

O Springdoc expõe o JSON em `/api/v1/api-docs` e o Swagger UI em `/swagger-ui/**`. O path `/api/v1/api-docs/**` está correto para a documentação, mas o UI precisa de `/v3/api-docs/**` liberado também para funcionar sem autenticação.

**✅ Correção:**

```java
private static final String[] PUBLIC_PATHS = {
    "/api/v1/auth/**",
    "/actuator/health",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/api/v1/api-docs/**",
    "/v3/api-docs/**"   // ✅ adicionar — Springdoc usa este path interno
};
```

---

### ALERTA 3 — `application.yml` — `spring.jpa.properties` com chaves erradas (linhas 22–23)

```yaml
properties:
  hibernate:
    format_sql: true
    dialect: org.hibernate.dialect.PostgreSQLDialect
    jdbc:
      batch_size: 25
    order_inserts: true  # ❌ deve estar dentro de hibernate.jdbc, não de hibernate
```

`hibernate.order_inserts` deve ser `hibernate.jdbc.order_inserts` para funcionar com batch insert.

**✅ Correção:**

```yaml
properties:
  hibernate:
    format_sql: true
    dialect: org.hibernate.dialect.PostgreSQLDialect
    jdbc:
      batch_size: 25
      order_inserts: true   # ✅ movido para dentro de jdbc
      order_updates: true   # ✅ recomendado junto com batch
```

---

### ALERTA 4 — `Approval.java` — `createdAt`/`updatedAt` manuais conflitam com `BaseEntity` (linhas 43–47)

```java
@Column(name = "created_at")
private Instant createdAt = Instant.now();  // ❌ duplica o campo de BaseEntity

@Column(name = "updated_at")
private Instant updatedAt = Instant.now();
```

`Approval` **não** estende `BaseEntity`, mas declara os mesmos campos. O problema é que `updatedAt` nunca é atualizado automaticamente (sem `@LastModifiedDate` + `@EntityListeners`).

**✅ Correção:** fazer `Approval extends BaseEntity` (ou adicionar `@EntityListeners(AuditingEntityListener.class)` + anotações de auditoria).

---

## 🔵 CODE SMELLS (BAIXO)

---

### CS 1 — `AsyncConfig.java` — `@EnableAsync` duplicado

`@EnableAsync` já está na classe principal `AustralEstoqueApplication`. Tê-lo também em `AsyncConfig` é redundante (mas não causa erro).

**✅ Correção:** remover `@EnableAsync` de `AsyncConfig`.

---

### CS 2 — `UserDetailsServiceImpl.java` — import `Collectors` não utilizado (linha 13)

```java
import java.util.stream.Collectors; // ❌ não usado — .collect(Collectors.toSet()) foi removido
```

A linha `collect(Collectors.toSet())` existe no código, mas o resultado é atribuído a `var authorities`. Se compilar com warnings habilitados, gerará aviso de import não utilizado.

> Na verdade `Collectors` **é** usado na linha 33. Este CS não se aplica — import está correto.

---

### CS 3 — `ItemRepository.java` — JPQL faz JOIN em `StockBalance` mas a entidade pode não estar mapeada (linha 29)

```java
@Query("SELECT i FROM Item i JOIN StockBalance sb ON sb.item = i WHERE ...")
List<Item> findItemsBelowMinimumStock();
```

Se `StockBalance` não existir como entidade JPA mapeada no pacote `domain/stock`, essa query lançará `QueryException` ao inicializar o contexto.

**✅ Verificação:** confirmar que existe `com.austral.estoque.domain.stock.StockBalance` com `@Entity`. Caso contrário, remover ou comentar o método até a entidade ser criada.

---

## 📋 Checklist de Correções por Prioridade

### Fase 1 — Corrigir antes de qualquer build (erros de compilação)

- [ ] `JwtTokenProvider.java` — Remover chamada de `initKey()` no construtor  
- [ ] `JwtTokenProvider.java` — Migrar API JJWT para versão 0.12.x (builder fluente)  
- [ ] `JwtTokenProvider.java` — Adicionar import `io.jsonwebtoken.JwtException`  
- [ ] `JwtTokenProvider.java` — Corrigir `throw e` fora do catch (linha 126)  
- [ ] `AsyncConfig.java` — Remover import e uso de `org.springframework.util.LogFactory`  
- [ ] `AsyncConfig.java` — Adicionar `@Slf4j` e usar `log.warn()`  
- [ ] `OrderService.java` — Adicionar `public` na declaração da classe  
- [ ] `OrderService.java` — Corrigir assinatura de `findByCode` (retorno de `Optional`)  
- [ ] `application.yml` — Adicionar `spring:` na primeira linha  

### Fase 2 — Corrigir antes de testar em ambiente real

- [ ] `application.yml` — Corrigir typo `JWT_REFRESH_EXPIRATION_EXPIRATION_MS`  
- [ ] `AuthService.java` — Persistir `Role` antes de salvar `User` no seed  
- [ ] `application.yml` — Mover `order_inserts` para dentro de `hibernate.jdbc`  
- [ ] `SecurityConfig.java` — Adicionar `/v3/api-docs/**` em `PUBLIC_PATHS`  

### Fase 3 — Refatoração recomendada

- [ ] `ItemController.java` — Criar `ItemService` e remover acesso direto ao repositório  
- [ ] `Order.java` — Remover acento do enum `DEPÓSITO`  
- [ ] `Approval.java` — Estender `BaseEntity` ou adicionar listeners de auditoria  
- [ ] `ItemRepository.java` — Verificar existência da entidade `StockBalance`  
- [ ] `AsyncConfig.java` — Remover `@EnableAsync` duplicado  

---

## 🗂 Inventário de Arquivos Analisados

| Arquivo | Status |
|---|---|
| `AustralEstoqueApplication.java` | ✅ OK |
| `config/SecurityConfig.java` | 🟡 1 alerta |
| `config/AsyncConfig.java` | 🔴 2 erros críticos |
| `config/AuditConfig.java` | ✅ OK |
| `security/JwtTokenProvider.java` | 🔴 3 erros críticos |
| `security/JwtAuthFilter.java` | ✅ OK (depende de JwtTokenProvider corrigido) |
| `security/UserDetailsServiceImpl.java` | ✅ OK |
| `service/AuthService.java` | 🟠 1 erro de lógica |
| `service/order/OrderService.java` | 🔴🟠 2 erros |
| `controller/AuthController.java` | ✅ OK |
| `controller/ItemController.java` | 🟡 1 alerta |
| `controller/order/OrderController.java` | ✅ OK |
| `domain/common/BaseEntity.java` | ✅ OK |
| `domain/user/User.java` | ✅ OK |
| `domain/user/Role.java` | ✅ OK (falta `@Builder`) |
| `domain/item/Item.java` | ✅ OK |
| `domain/order/Order.java` | 🟡 1 alerta (enum com acento) |
| `domain/requisition/Requisition.java` | ✅ OK |
| `domain/approval/Approval.java` | 🟡 1 alerta (auditoria manual) |
| `domain/approval/ApprovalTier.java` | ✅ OK |
| `repository/user/UserRepository.java` | ✅ OK |
| `repository/item/ItemRepository.java` | 🔵 1 code smell |
| `exception/GlobalExceptionHandler.java` | ✅ OK |
| `exception/BusinessException.java` | ✅ OK |
| `exception/ResourceNotFoundException.java` | ✅ OK |
| `resources/application.yml` | 🔴🟡 3 problemas |
| `pom.xml` | ✅ OK |
| `.env.example` | ✅ OK |
