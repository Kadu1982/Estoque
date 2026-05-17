package com.austral.estoque.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequest(
    @NotBlank String code,
    @NotNull UUID supplierId,
    UUID requesterId,
    UUID requisitionId,
    String notes,
    @Valid List<OrderItemRequest> items
) {
}
