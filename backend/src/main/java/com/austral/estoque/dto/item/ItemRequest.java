package com.austral.estoque.dto.item;

import com.austral.estoque.domain.item.Item;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequest(
    String code,
    String description,
    UUID categoryId,
    String unitOfMeasure,
    String brand,
    String specification,
    Item.Criticality criticality,
    BigDecimal minStock,
    BigDecimal maxStock,
    Integer leadTimeDays,
    Boolean active
) {
}
