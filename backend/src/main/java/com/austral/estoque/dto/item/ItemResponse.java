package com.austral.estoque.dto.item;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemResponse(
    UUID id,
    String code,
    String description,
    String brand,
    String unitOfMeasure,
    String criticality,
    BigDecimal minStock,
    BigDecimal maxStock,
    Integer leadTimeDays,
    boolean active,
    CategoryResponse category
) {
    public record CategoryResponse(UUID id, String name) {}
}
