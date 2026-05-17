package com.austral.estoque.dto.stock;

import java.math.BigDecimal;
import java.util.UUID;

public record StockBalanceResponse(
    UUID id,
    UUID itemId,
    String itemCode,
    String itemDescription,
    UUID warehouseId,
    String warehouseName,
    BigDecimal quantity,
    BigDecimal minStock,
    BigDecimal maxStock,
    String unitOfMeasure
) {
}
