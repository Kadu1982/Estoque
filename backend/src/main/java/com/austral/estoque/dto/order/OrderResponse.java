package com.austral.estoque.dto.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String code,
    String status,
    String notes,
    SupplierSummary supplier,
    RequesterSummary requester,
    List<OrderItemResponse> items
) {
    public record SupplierSummary(UUID id, String name) {}
    public record RequesterSummary(UUID id, String username, String fullName) {}
    public record OrderItemResponse(
        UUID id,
        UUID itemId,
        String itemCode,
        String itemDescription,
        BigDecimal quantity,
        BigDecimal receivedQuantity,
        BigDecimal unitPrice,
        BigDecimal total
    ) {}
}
