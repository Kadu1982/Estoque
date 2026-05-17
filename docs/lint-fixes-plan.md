# 🔧 Plano de Correção de Erros de Lint — Austral Estoque Backend

> **Escopo:** análise estática de todos os arquivos Java do projeto  
> **Stack:** Spring Boot 3.3.5 / Java 21 / Lombok  
> **Executor:** LLM — aplica as correções descritas aqui, arquivo por arquivo

---

## Resumo dos Problemas Encontrados

| # | Severidade | Arquivo | Problema |
|---|---|---|---|
| L1 | 🔴 Erro de compilação | `OrderService.java` | Importa `OrderStatus` top-level que conflita com `Order.OrderStatus` |
| L2 | 🔴 Erro de compilação | `OrderRepository.java` | `countByStatus` usa `Order.OrderStatus` mas existe `OrderStatus` top-level com valores diferentes |
| L3 | 🟠 Arquitetura / Lint | `ItemController.java` | Injeta `ItemRepository` diretamente em vez de um Service |
| L4 | 🟠 Arquitetura / Lint | `SupplierController.java` | Injeta `SupplierRepository` diretamente em vez de um Service |
| L5 | 🟡 Import não utilizado | `OrderService.java` | Imports de `OrderItem`, `Requisition`, `BigDecimal` nunca usados |
| L6 | 🟡 Import não utilizado | `OrderItemController.java` | Import de `Optional` nunca utilizado diretamente |
| L7 | 🟡 Import não utilizado | `OrderController.java` | Import de `Optional` nunca utilizado diretamente |
| L8 | 🟡 Entidade sem auditoria | `Approval.java` | `createdAt`/`updatedAt` manuais — não estende `BaseEntity` |
| L9 | 🟡 Entidade sem auditoria | `OrderItem.java` | `createdAt` manual — não estende `BaseEntity` |
| L10 | 🔵 Wildcard import | `Order.java` | `import com.austral.estoque.domain.organization.*` (não sabe o que usa) |
| L11 | 🔵 Wildcard import | `Requisition.java` | `import com.austral.estoque.domain.organization.*` (não sabe o que usa) |
| L12 | 🔵 Código inline | `SupplierController.java` | Método `update` com múltiplos setters na mesma linha (linha 49-53) |
| L13 | 🔵 @EnableAsync duplo | `AsyncConfig.java` | `@EnableAsync` já declarado na classe principal |
| L14 | 🔵 Raw type | `OrderController.java` | `ResponseEntity<?>` com raw wildcard sem justificativa |

---

## L1 + L2 — `OrderStatus` Duplicado e Conflitante

### Problema

Existem **dois** `OrderStatus` no projeto:

1. **`Order.OrderStatus`** (dentro de `Order.java`, linha 60) com valores:
   `RASCUNHO, CONFIRMADO, EM_PRODUCAO, PRONTOS_PARA_ENTREGA, ENTREGUE, PARCIALMENTE_RECEBIDO, CANCELADO`

2. **`OrderStatus.java`** (arquivo próprio, `domain/order/OrderStatus.java`) com valores:
   `PENDING, PROCESSED, DELIVERED, CANCELLED`

`OrderService.java` importa `com.austral.estoque.domain.order.OrderStatus` (o top-level) e o passa para `orderRepository.countByStatus(Order.OrderStatus status)`, que espera o `Order.OrderStatus` interno. **Isso gera erro de compilação por tipo incompatível.**

### Arquivo: `OrderService.java` — Correção dos imports

**Remover** da linha 5:
```java
import com.austral.estoque.domain.order.OrderStatus;  // ❌ remover
```

**Alterar** a assinatura do método `countByStatus` (linha 35) de:
```java
public long countByStatus(com.austral.estoque.domain.order.Order.OrderStatus status) {
```
Para:
```java
public long countByStatus(Order.OrderStatus status) {
```

### Arquivo: `OrderStatus.java` — Deletar o arquivo

O arquivo `backend/src/main/java/com/austral/estoque/domain/order/OrderStatus.java` deve ser **deletado**.
O enum correto e em uso é o inner enum `Order.OrderStatus` definido dentro de `Order.java`.

### Arquivo: `OrderController.java` — Atualizar referência na linha 40

**Antes:**
```java
return orderService.countByStatus(com.austral.estoque.domain.order.Order.OrderStatus.valueOf(status));
```

**Depois:**
```java
return orderService.countByStatus(Order.OrderStatus.valueOf(status));
```
O import `import com.austral.estoque.domain.order.Order;` já existe na linha 3.

---

## L3 — `ItemController.java` — Acessando Repository Diretamente

### Problema

`ItemController` injeta `ItemRepository` (linha 22). Controllers não devem acessar repositórios — viola camada de serviço e causa `LazyInitializationException` ao serializar entidades com `FETCH.LAZY`.

### Solução: Criar `ItemService.java`

**Criar** `backend/src/main/java/com/austral/estoque/service/ItemService.java`:

```java
package com.austral.estoque.service;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public Page<Item> list(String search, UUID categoryId, Boolean active, Pageable pageable) {
        return itemRepository.search(search, categoryId, active, pageable);
    }

    public Item findById(UUID id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    @Transactional
    public Item create(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(UUID id, Item body) {
        Item item = findById(id);
        item.setDescription(body.getDescription());
        item.setBrand(body.getBrand());
        item.setSpecification(body.getSpecification());
        item.setCriticality(body.getCriticality());
        item.setMinStock(body.getMinStock());
        item.setMaxStock(body.getMaxStock());
        item.setLeadTimeDays(body.getLeadTimeDays());
        item.setUnitOfMeasure(body.getUnitOfMeasure());
        item.setActive(body.isActive());
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(UUID id) {
        Item item = findById(id);
        item.softDelete();
        itemRepository.save(item);
    }
}
```

### Atualizar `ItemController.java`

Substituir o conteúdo completo do controller:

```java
package com.austral.estoque.controller;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Page<Item> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) Boolean active,
        @PageableDefault(size = 20, sort = "description") Pageable pageable
    ) {
        return itemService.list(search, categoryId, active, pageable);
    }

    @GetMapping("/{id}")
    public Item getById(@PathVariable UUID id) {
        return itemService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE')")
    public ResponseEntity<Item> create(@RequestBody Item item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE')")
    public Item update(@PathVariable UUID id, @RequestBody Item body) {
        return itemService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## L4 — `SupplierController.java` — Acessando Repository Diretamente

### Solução: Criar `SupplierService.java`

**Criar** `backend/src/main/java/com/austral/estoque/service/SupplierService.java`:

```java
package com.austral.estoque.service;

import com.austral.estoque.domain.supplier.Supplier;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Page<Supplier> list(String search, Pageable pageable) {
        return supplierRepository.search(search, pageable);
    }

    public Supplier findById(UUID id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(UUID id, Supplier body) {
        Supplier s = findById(id);
        s.setName(body.getName());
        s.setType(body.getType());
        s.setCountry(body.getCountry());
        s.setCurrency(body.getCurrency());
        s.setPaymentTermDays(body.getPaymentTermDays());
        s.setContactName(body.getContactName());
        s.setContactEmail(body.getContactEmail());
        s.setContactPhone(body.getContactPhone());
        s.setContactWhatsapp(body.getContactWhatsapp());
        s.setNotes(body.getNotes());
        s.setActive(body.isActive());
        return supplierRepository.save(s);
    }

    @Transactional
    public void delete(UUID id) {
        Supplier s = findById(id);
        s.softDelete();
        supplierRepository.save(s);
    }
}
```

### Atualizar `SupplierController.java`

```java
package com.austral.estoque.controller;

import com.austral.estoque.domain.supplier.Supplier;
import com.austral.estoque.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public Page<Supplier> list(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return supplierService.list(search, pageable);
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable UUID id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRADOR')")
    public ResponseEntity<Supplier> create(@RequestBody Supplier supplier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRADOR')")
    public Supplier update(@PathVariable UUID id, @RequestBody Supplier body) {
        return supplierService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## L5 — `OrderService.java` — Imports Não Utilizados

**Remover** as seguintes linhas de import (nunca são referenciados no corpo do arquivo):

```java
import com.austral.estoque.domain.order.OrderItem;    // ❌ remover
import com.austral.estoque.domain.order.OrderStatus;  // ❌ remover (L1 também)
import com.austral.estoque.domain.requisition.Requisition; // ❌ remover
import java.math.BigDecimal;                          // ❌ remover
```

---

## L6 + L7 — `Optional` Não Utilizado em Controllers

Em `OrderController.java` e `OrderItemController.java`, o import:
```java
import java.util.Optional;
```
**Não é utilizado diretamente** (o `Optional` é manipulado via retorno de `orderService.findById()` encadeado com `.map().orElseGet()`). O import pode ser removido sem problema — Java não requer import explícito quando o tipo não aparece no código-fonte do arquivo chamador.

**Em `OrderController.java`**, remover linha 10:
```java
import java.util.Optional; // ❌ remover
```

**Em `OrderItemController.java`**, remover linha 10:
```java
import java.util.Optional; // ❌ remover
```

---

## L8 — `Approval.java` — Auditoria Manual sem `BaseEntity`

### Problema
`Approval` declara `createdAt` e `updatedAt` manualmente (linhas 43-47) com valor fixo `Instant.now()`. O campo `updatedAt` **nunca será atualizado automaticamente**.

### Correção

**Fazer `Approval` estender `BaseEntity`** e remover os campos manuais:

```java
// Remover o import de Instant se não houver outros usos
// Remover o import de UUID (já vem do BaseEntity via herança JPA)

@Entity @Table(name = "approvals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Approval extends BaseEntity {   // ← adicionar extends BaseEntity

    // ... demais campos mantidos ...

    @Column(name = "decision_at")
    private Instant decisionAt;             // ← manter, é campo de negócio

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    // ❌ REMOVER as linhas abaixo:
    // @Column(name = "created_at")
    // private Instant createdAt = Instant.now();
    // @Column(name = "updated_at")
    // private Instant updatedAt = Instant.now();

    public enum ApprovalStatus { PENDENTE, APROVADA, REPROVADA, ESCALADA }
}
```

**Imports a remover** (se `decisionAt` e `slaDeadline` ainda usam `Instant`, manter o import):
```java
import java.util.UUID;  // ❌ remover — vem do BaseEntity
```

---

## L9 — `OrderItem.java` — `createdAt` Manual sem `BaseEntity`

### Problema
`OrderItem` não estende `BaseEntity` e declara `createdAt` manualmente (linha 41-42) com `Instant.now()` fixo. Sem `@EntityListeners(AuditingEntityListener.class)`, o valor nunca é atualizado.

### Correção

**Fazer `OrderItem` estender `BaseEntity`** e remover campo manual:

```java
@Entity @Table(name = "order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItem extends BaseEntity {   // ← adicionar extends BaseEntity

    // ❌ REMOVER:
    // @Id @GeneratedValue(strategy = GenerationType.UUID)
    // private UUID id;
    // (o id vem do BaseEntity)

    // ❌ REMOVER:
    // @Column(name = "created_at")
    // private Instant createdAt = Instant.now();

    // ... demais campos mantidos ...
}
```

**Imports a remover:**
```java
import java.time.Instant; // ❌ remover — não há mais uso
import java.util.UUID;    // ❌ remover — vem do BaseEntity
```

---

## L10 + L11 — Wildcard Imports de `organization`

### `Order.java` (linha 5)
```java
import com.austral.estoque.domain.organization.*; // ❌ wildcard
```
Após análise do código, `Order.java` **não usa nenhuma classe** de `organization` diretamente (sem campos do tipo `OperationalUnit`, `Sector`, etc.). 

**Correção:** Remover o import completamente.

### `Requisition.java` (linha 4)
```java
import com.austral.estoque.domain.organization.*; // ❌ wildcard
```
`Requisition.java` usa: `OperationalUnit`, `Warehouse`, `Sector`, `CostCenter`.

**Substituir** pelo import explícito:
```java
import com.austral.estoque.domain.organization.CostCenter;
import com.austral.estoque.domain.organization.OperationalUnit;
import com.austral.estoque.domain.organization.Sector;
import com.austral.estoque.domain.organization.Warehouse;
```

---

## L12 — `SupplierController.java` — Múltiplos Setters por Linha

**Antes (linhas 49-53):**
```java
s.setName(body.getName()); s.setType(body.getType()); s.setCountry(body.getCountry());
s.setCurrency(body.getCurrency()); s.setPaymentTermDays(body.getPaymentTermDays());
// ...
```

**Correção:** Esse problema é resolvido automaticamente ao mover a lógica para `SupplierService` (veja L4). O controller não terá mais setters.

---

## L13 — `AsyncConfig.java` — `@EnableAsync` Duplicado

`@EnableAsync` já está em `AustralEstoqueApplication.java`. Ter em `AsyncConfig` também é redundante.

**Remover** a anotação `@EnableAsync` da linha 15 de `AsyncConfig.java`:
```java
@Configuration
// @EnableAsync  ← ❌ remover esta linha
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
```

---

## L14 — `OrderController.java` — Raw Wildcard `ResponseEntity<?>`

### Problema
`ResponseEntity<?>` na linha 20 e 32 é aceitável para retornos polimórficos, mas pode ser mais específico.

### Correção
```java
// Antes:
public ResponseEntity<?> findById(@PathVariable UUID id) {

// Depois:
public ResponseEntity<Order> findById(@PathVariable UUID id) {
    return orderService.findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```
Repetir para `findByCode`:
```java
public ResponseEntity<Order> findByCode(@PathVariable String code) {
    return orderService.findByCode(code)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

## Ordem de Execução para o LLM

Execute as correções nesta sequência para evitar dependências quebradas:

1. **Deletar** `OrderStatus.java` (L1/L2)
2. **Criar** `ItemService.java` (L3)
3. **Criar** `SupplierService.java` (L4)
4. **Modificar** `OrderService.java` — remover imports (L1, L5)
5. **Modificar** `ItemController.java` — usar `ItemService` (L3)
6. **Modificar** `SupplierController.java` — usar `SupplierService` (L4, L12)
7. **Modificar** `OrderController.java` — remover import `Optional`, corrigir `OrderStatus`, tipificar `ResponseEntity` (L1, L7, L14)
8. **Modificar** `OrderItemController.java` — remover import `Optional` (L6)
9. **Modificar** `Approval.java` — estender `BaseEntity`, remover campos manuais (L8)
10. **Modificar** `OrderItem.java` — estender `BaseEntity`, remover campos manuais (L9)
11. **Modificar** `Order.java` — remover wildcard import `organization` (L10)
12. **Modificar** `Requisition.java` — substituir wildcard por imports explícitos (L11)
13. **Modificar** `AsyncConfig.java` — remover `@EnableAsync` (L13)

---

## Checklist de Verificação Pós-Correção

- [ ] `OrderStatus.java` deletado — não existe mais `domain/order/OrderStatus.java`
- [ ] `OrderService` compila sem erro de tipo em `countByStatus`
- [ ] `ItemController` não importa mais `ItemRepository`
- [ ] `SupplierController` não importa mais `SupplierRepository`
- [ ] Zero imports `java.util.Optional` em controllers que não declaram `Optional<T>` explicitamente
- [ ] `Approval` e `OrderItem` estendem `BaseEntity`
- [ ] Nenhum wildcard `organization.*` no código
- [ ] `AsyncConfig` sem `@EnableAsync`
- [ ] `ResponseEntity<Order>` em todos os endpoints do `OrderController`
