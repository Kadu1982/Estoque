package com.austral.estoque.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderReceiptRequest(
    @NotNull UUID warehouseId,
    @NotNull UUID receiverId,
    @NotNull List<ReceiptItemRequest> items
) {
    public record ReceiptItemRequest(
        @NotNull UUID orderItemId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity
    ) {
    }
}
