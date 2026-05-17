---
tags:
  - java21
  - spring-boot
  - performance
  - escalabilidade
  - segurança
  - virtual-threads
  - austral
status: backlog
criado: 2026-05-13
prioridade: alta
---

# 🚀 Plano de Melhorias — Java 21 / Spring Boot 3 · Austral Estoque

> **Objetivo:** Elevar escalabilidade, segurança e performance do backend usando os recursos modernos do Java 21 e Spring Boot 3.x, sem reescrever o sistema — são melhorias incrementais e seguras.

---

## 📋 Diagnóstico do Projeto Atual

| Aspecto | Estado Atual | Problema Identificado |
|---|---|---|
| Modelo de threads | Servlet tradicional (Tomcat + thread pool) | Threads bloqueantes em I/O (DB, Redis, HTTP externo) |
| Async | `@EnableAsync` ativo, mas sem executor configurado | Usa `SimpleAsyncTaskExecutor` por padrão — sem limites |
| JPA / Hikari | `maximum-pool-size: 20` | Pool sem tuning para Virtual Threads |
| JWT | `getSigningKey()` reconstrói a chave a cada chamada | Overhead desnecessário por requisição |
| Segurança HTTP | CSRF desativado, `allowedHeaders: "*"` | Configuração permissiva demais para produção |
| Actuator | Expõe `health, info, metrics` sem auth | Informação de infra exposta publicamente |
| Docker (JVM) | `java -jar app.jar` simples | Sem flags de otimização para container |
| Controller → Repository | `ItemController` chama `ItemRepository` diretamente | Viola camada de serviço; dificulta cache e testes |
| Refresh Token | Refresh token não é invalidado no logout | Token roubado permanece válido por 7 dias |

---

## 🗺️ Roadmap de Melhorias (Por Prioridade)

### 🔴 FASE 1 — Quick Wins de Alta Prioridade (1–2 dias)

#### 1.1 Habilitar Virtual Threads (Project Loom)

**Arquivo:** `src/main/resources/application.yml`

Virtual Threads substituem o pool de threads do Tomcat por threads leves gerenciadas pela JVM. Com o stack atual (Spring MVC + JPA + Redis), a aplicação é **I/O-bound** — exatamente o perfil ideal para Loom. Não exige mudança de código de negócio.

```yaml
# Adicionar ao application.yml
spring:
  threads:
    virtual:
      enabled: true  # Liga Virtual Threads no Tomcat automaticamente (Spring Boot 3.2+)
```

> ✅ **Uma linha. Zero mudança de código de negócio.** Capacidade de conexões simultâneas passa de ~200 (Tomcat padrão) para dezenas de milhares.

**Atenção — ajuste obrigatório no Hikari:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20    # NÃO aumentar absurdamente — PostgreSQL tem limite
      minimum-idle: 10
      connection-timeout: 10000  # Reduzir: VT falha rápido em vez de bloquear
      keepalive-time: 30000
```

> ⚠️ Com Virtual Threads, o gargalo migra para o pool de conexões do banco. 20–30 conexões é suficiente para PostgreSQL. **Não coloque 200+.**

---

#### 1.2 Corrigir Executor Async

**Arquivo:** `src/main/java/com/austral/estoque/config/AsyncConfig.java` *(novo)*

`@EnableAsync` está ativo, mas sem `TaskExecutor` configurado. Com Virtual Threads habilitadas, o correto é usar `VirtualThreadTaskExecutor`:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        // Explicitamente usa Virtual Threads para tarefas @Async
        return new VirtualThreadTaskExecutor("async-vt-");
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
```

> Garante que chamadas `@Async` (notificações de email, WhatsApp, SMS) usem Virtual Threads e não bloqueiem o pool principal.

---

#### 1.3 Cache da Signing Key no JWT

**Arquivo:** `JwtTokenProvider.java`

Atualmente `getSigningKey()` faz `jwtSecret.getBytes()` + `Keys.hmacShaKeyFor()` **em toda validação de JWT**. Com alta concorrência via Virtual Threads, isso multiplica o custo desnecessariamente.

```java
// ANTES (problemático — reconstrói a cada chamada):
private SecretKey getSigningKey() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
}

// DEPOIS (inicializa uma vez no startup):
private SecretKey signingKey;

@PostConstruct
private void initKey() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}

private SecretKey getSigningKey() {
    return this.signingKey;
}
```

---

### 🟡 FASE 2 — Segurança (2–3 dias)

#### 2.1 Blacklist de Refresh Tokens no Redis

**Arquivo:** `AuthService.java` + novo `TokenBlacklistService.java`

**Problema atual:** O `refresh()` não tem logout real — o token roubado vale até expirar (7 dias por padrão).

```java
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "jwt:blacklist:";

    public void blacklist(String token, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + token, "revoked", ttl);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
```

**Integrar no `JwtAuthFilter.java`:**
```java
// Após extrair username, antes de autenticar:
if (tokenBlacklistService.isBlacklisted(jwt)) {
    filterChain.doFilter(request, response);
    return;
}
```

**Adicionar `logout()` no `AuthService.java`:**
```java
public void logout(String refreshToken) {
    Date expiry = jwtTokenProvider.extractExpiration(refreshToken);
    Duration ttl = Duration.between(Instant.now(), expiry.toInstant());
    if (!ttl.isNegative()) {
        tokenBlacklistService.blacklist(refreshToken, ttl);
    }
}
```

**Adicionar endpoint no `AuthController.java`:**
```java
@PostMapping("/logout")
public ResponseEntity<Void> logout(@RequestBody LogoutRequest req) {
    authService.logout(req.refreshToken());
    return ResponseEntity.noContent().build();
}
```

---

#### 2.2 Reforçar Headers HTTP de Segurança

**Arquivo:** `SecurityConfig.java`

```java
http
    .headers(headers -> headers
        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
        .contentTypeOptions(withDefaults())
        .httpStrictTransportSecurity(hsts -> hsts
            .includeSubDomains(true)
            .maxAgeInSeconds(31536000)
        )
        .referrerPolicy(referrer ->
            referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        .permissionsPolicy(permissions ->
            permissions.policy("camera=(), microphone=(), geolocation=()"))
    );
```

---

#### 2.3 Rate Limiting no Endpoint de Login

**Arquivo:** `AuthController.java` + `pom.xml`

Previne brute-force sem depender de Nginx:

```xml
<!-- pom.xml -->
<dependency>
  <groupId>com.bucket4j</groupId>
  <artifactId>bucket4j-core</artifactId>
  <version>8.10.1</version>
</dependency>
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
</dependency>
```

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req,
                                               HttpServletRequest httpReq) {
        Bucket bucket = buckets.get(httpReq.getRemoteAddr(), ip ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build()
        );
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return ResponseEntity.ok(authService.login(req));
    }
}
```

> Limita a 10 tentativas/minuto por IP. Configurável por ambiente.

---

#### 2.4 Proteger o Actuator

**Arquivo:** `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /internal/actuator  # Mudar path padrão
  endpoint:
    health:
      show-details: when-authorized
  server:
    port: 9090  # Porta separada — bloquear no Nginx/firewall
```

> No Nginx, adicione `deny all;` para a porta 9090 no bloco externo.

---

### 🟢 FASE 3 — Performance e Escalabilidade (3–5 dias)

#### 3.1 Adicionar Service Layer (Refactor Arquitetural)

**Problema:** `ItemController` chama `ItemRepository` diretamente, violando separação de responsabilidades e impedindo cache, testes unitários limpos e reuso de lógica.

```java
// ItemService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    @Cacheable(value = "items", key = "#id")
    public Item findById(UUID id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    public Page<Item> search(String query, UUID categoryId, Boolean active, Pageable pageable) {
        return itemRepository.search(query, categoryId, active, pageable);
    }

    @Transactional
    @CacheEvict(value = "items", key = "#result.id")
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    @CacheEvict(value = "items", key = "#id")
    public void softDelete(UUID id) {
        Item item = findById(id);
        item.softDelete();
        itemRepository.save(item);
    }
}
```

---

#### 3.2 Records Java 21 para DTOs

**Arquivos:** todos em `dto/`

Substituir classes DTO com boilerplate Lombok por Records imutáveis — mais seguros em ambientes concorrentes:

```java
// ANTES (classe com Lombok):
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String login;
    private String password;
}

// DEPOIS (Java 21 Record com Bean Validation):
public record LoginRequest(
    @NotBlank String login,
    @NotBlank String password
) {}

// AuthResponse como Record:
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    UUID userId,
    String username,
    String fullName,
    String email,
    List<String> roles
) {}
```

---

#### 3.3 Structured Concurrency para Notificações Multi-Canal

**Arquivo:** serviço de notificações (a ser criado)

Quando o sistema envia email + WhatsApp + SMS simultaneamente, use `StructuredTaskScope` para coordenar as chamadas de forma segura:

```java
// Requer --enable-preview no Java 21
public void notificar(String destino, String mensagem) throws InterruptedException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var emailTask    = scope.fork(() -> emailService.enviar(destino, mensagem));
        var whatsappTask = scope.fork(() -> whatsappService.enviar(destino, mensagem));
        var smsTask      = scope.fork(() -> smsService.enviar(destino, mensagem));

        scope.join().throwIfFailed();

        log.info("Notificações enviadas: email={}, wa={}, sms={}",
            emailTask.get(), whatsappTask.get(), smsTask.get());
    }
}
```

**Habilitar no `maven-compiler-plugin`:**
```xml
<compilerArgs>
  <arg>--enable-preview</arg>
</compilerArgs>
```

> 💡 Em Java 25 (próximo LTS), Structured Concurrency será estável sem `--enable-preview`.

---

#### 3.4 Otimizar Dockerfile com JVM Flags para Container + Segurança

```dockerfile
# ── Build stage ───────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Runtime stage ─────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl

# Segurança: rodar como usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/estoque-*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

**Flags explicadas:**
- `-XX:+UseContainerSupport` → JVM respeita cgroups do Docker (não lê RAM do host)
- `-XX:MaxRAMPercentage=75.0` → Usa 75% da RAM alocada ao container
- `-XX:+UseZGC -XX:+ZGenerational` → GC de baixa latência (< 1ms de pausa), ideal com Virtual Threads
- `-Djava.security.egd=...` → Geração de entropia mais rápida (evita travamento na inicialização)

---

#### 3.5 Sealed Classes para Hierarquia de Erros

**Arquivo:** `exception/DomainError.java` *(novo)*

Em vez de exceções genéricas, use Sealed Classes com Pattern Matching (Java 21):

```java
public sealed interface DomainError
    permits DomainError.NotFound, DomainError.BusinessViolation, DomainError.Unauthorized {

    record NotFound(String entity, Object id) implements DomainError {}
    record BusinessViolation(String message) implements DomainError {}
    record Unauthorized(String reason) implements DomainError {}
}
```

**Usar em `@ExceptionHandler` com switch expression exhaustivo:**
```java
private ResponseEntity<ProblemDetail> handle(DomainError error) {
    return switch (error) {
        case DomainError.NotFound(var entity, var id) ->
            ResponseEntity.status(404).body(
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                    entity + " não encontrado: " + id));
        case DomainError.BusinessViolation(var msg) ->
            ResponseEntity.status(422).body(
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, msg));
        case DomainError.Unauthorized(var reason) ->
            ResponseEntity.status(403).body(
                ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, reason));
    };
}
```

---

### 🔵 FASE 4 — Observabilidade (2 dias)

#### 4.1 Micrometer + Prometheus + Grafana

**`pom.xml`:**
```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**`docker-compose.yml`:**
```yaml
prometheus:
  image: prom/prometheus:v2.50.0
  volumes:
    - ./infra/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
  networks:
    - austral-net

grafana:
  image: grafana/grafana:10.4.0
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin}
  ports:
    - "3001:3000"
  networks:
    - austral-net
```

**`infra/prometheus/prometheus.yml`:**
```yaml
scrape_configs:
  - job_name: 'austral-api'
    metrics_path: '/internal/actuator/prometheus'
    static_configs:
      - targets: ['api:9090']
```

---

#### 4.2 Correlation ID / Distributed Tracing

**Arquivo:** `filter/CorrelationIdFilter.java` *(novo)*

```java
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String correlationId = Optional.ofNullable(req.getHeader(CORRELATION_ID_HEADER))
            .orElse(UUID.randomUUID().toString());

        MDC.put("correlationId", correlationId);
        res.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear(); // CRÍTICO com Virtual Threads — sempre limpar o contexto
        }
    }
}
```

> ⚠️ **Atenção com Virtual Threads:** `ThreadLocal` (usado pelo MDC do Logback) é herdado pela thread virtual filho. Sempre faça `MDC.clear()` no `finally` para evitar vazamento de contexto entre requests.

---

## 📦 Resumo de Dependências a Adicionar

```xml
<!-- Rate Limiting -->
<dependency>
  <groupId>com.bucket4j</groupId>
  <artifactId>bucket4j-core</artifactId>
  <version>8.10.1</version>
</dependency>
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
</dependency>

<!-- Prometheus Metrics -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## ✅ Checklist de Implementação

### Fase 1 — Quick Wins (Prioridade Máxima)
- [ ] Habilitar `spring.threads.virtual.enabled: true` no `application.yml`
- [ ] Ajustar Hikari: `connection-timeout: 10000`, `minimum-idle: 10`
- [ ] Criar `AsyncConfig.java` com `VirtualThreadTaskExecutor`
- [ ] Adicionar `@PostConstruct` no `JwtTokenProvider` para cachear `signingKey`

### Fase 2 — Segurança
- [ ] Criar `TokenBlacklistService.java` (usando Redis já disponível)
- [ ] Integrar blacklist no `JwtAuthFilter.java`
- [ ] Adicionar endpoint `POST /api/v1/auth/logout` no `AuthController`
- [ ] Adicionar headers de segurança no `SecurityConfig.java`
- [ ] Implementar Rate Limiting no endpoint de login (Bucket4j)
- [ ] Mover Actuator para porta separada (9090) e proteger no Nginx

### Fase 3 — Performance
- [ ] Criar `ItemService.java` com `@Cacheable` e `@CacheEvict`
- [ ] Refatorar `ItemController.java` para usar `ItemService`
- [ ] Converter DTOs para Java Records (`LoginRequest`, `AuthResponse`, etc.)
- [ ] Adicionar `--enable-preview` no `maven-compiler-plugin`
- [ ] Otimizar `Dockerfile` com JVM flags (ZGC, MaxRAMPercentage) e usuário não-root
- [ ] Avaliar Sealed Classes para hierarquia de exceções de domínio

### Fase 4 — Observabilidade
- [ ] Adicionar `micrometer-registry-prometheus` ao `pom.xml`
- [ ] Criar `infra/prometheus/prometheus.yml`
- [ ] Adicionar Prometheus + Grafana ao `docker-compose.yml`
- [ ] Criar `CorrelationIdFilter.java`

---

## 🎯 Impacto Esperado por Melhoria

| Melhoria | Métrica Esperada |
|---|---|
| Virtual Threads (Loom) | Throughput: 5–10x mais req/s com mesmo hardware |
| ZGC | Latência GC: < 1ms de pausa (vs 50–200ms com G1GC) |
| JWT Key Cache | CPU: elimina alocação desnecessária em todo request autenticado |
| Token Blacklist | Segurança: refresh tokens revogáveis no logout |
| Rate Limiting | Resistência a brute-force sem depender de infra externa |
| Hikari Tuning | Estabilidade sob alta carga sem timeout de pool |
| Service Layer + Cache | DB queries: redução de 40–70% para reads frequentes |
| Correlation ID | MTTR em produção: de minutos para segundos |

---

## 📚 Referências

- [JEP 444 — Virtual Threads (Java 21)](https://openjdk.org/jeps/444)
- [JEP 453 — Structured Concurrency (Java 21 Preview)](https://openjdk.org/jeps/453)
- [JEP 441 — Pattern Matching for switch](https://openjdk.org/jeps/441)
- [JEP 409 — Sealed Classes](https://openjdk.org/jeps/409)
- [Spring Boot 3.2 — Virtual Threads Support](https://spring.io/blog/2022/10/11/embracing-virtual-threads)
- [HikariCP + Virtual Threads](https://github.com/brettwooldridge/HikariCP/issues/2044)
- [Bucket4j Rate Limiting](https://bucket4j.com/)
- [ZGC Documentation](https://wiki.openjdk.org/display/zgc)
