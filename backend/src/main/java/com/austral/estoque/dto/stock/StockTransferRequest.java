package com.austral.estoque.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StockTransferRequest(
    @NotNull UUID sourceWarehouseId,
    @NotNull UUID targetWarehouseId,
    UUID costCenterId,
    @Valid @NotEmpty List<StockMovementItemRequest> items
) {
}
