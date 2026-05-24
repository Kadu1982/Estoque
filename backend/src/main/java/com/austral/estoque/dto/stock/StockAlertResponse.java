package com.austral.estoque.dto.stock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockAlertResponse(
    UUID id,
    UUID warehouseId,
    String warehouseName,
    UUID itemId,
    String itemCode,
    String itemDescription,
    BigDecimal quantityOnHand,
    BigDecimal plannedQuantity,
    BigDecimal percentageOfPlanned,
    String statusColor,
    String alertType,
    String alertStatus,
    Instant createdAt
) {
}
