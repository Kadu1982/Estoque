package com.austral.estoque.dto.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementItemRequest(
    @NotNull UUID itemId,
    @NotNull @DecimalMin(value = "0.001") BigDecimal quantity
) {
}
