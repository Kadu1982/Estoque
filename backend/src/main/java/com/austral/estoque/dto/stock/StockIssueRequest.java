package com.austral.estoque.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StockIssueRequest(
    @NotNull UUID warehouseId,
    UUID sectorId,
    UUID costCenterId,
    UUID assetId,
    UUID requestId,
    @Valid @NotEmpty List<StockMovementItemRequest> items
) {
}
