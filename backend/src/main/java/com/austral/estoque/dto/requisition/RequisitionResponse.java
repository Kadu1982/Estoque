package com.austral.estoque.dto.requisition;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RequisitionResponse(
    UUID id,
    String code,
    String status,
    String urgency,
    String justification,
    BigDecimal estimatedTotalUsd,
    Requester requester,
    List<RequisitionItemResponse> items
) {
    public record Requester(UUID id, String fullName) {}

    public record RequisitionItemResponse(
        UUID id,
        UUID itemId,
        String itemCode,
        String itemDescription,
        BigDecimal quantity,
        BigDecimal estimatedPriceUsd,
        BigDecimal estimatedTotalUsd
    ) {}
}
