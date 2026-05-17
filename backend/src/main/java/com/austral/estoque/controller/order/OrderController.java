package com.austral.estoque.controller.order;

import com.austral.estoque.domain.order.Order;
import com.austral.estoque.dto.order.OrderRequest;
import com.austral.estoque.dto.order.OrderReceiptRequest;
import com.austral.estoque.dto.order.OrderResponse;
import com.austral.estoque.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<OrderResponse> findByCode(@PathVariable String code) {
        return orderService.findByCode(code)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable UUID id, @RequestBody @Valid OrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<OrderResponse> receive(@PathVariable UUID id, @RequestBody @Valid OrderReceiptRequest request) {
        return ResponseEntity.ok(orderService.receiveOrder(id, request));
    }

    @GetMapping("/status/{status}")
    public long countByStatus(@PathVariable String status) {
        return orderService.countByStatus(Order.OrderStatus.valueOf(status));
    }
}
