package com.austral.estoque.dto.water;

import java.math.BigDecimal;
import java.util.UUID;

public record WaterWellResponse(
    UUID id,
    String name,
    UUID unitId,
    String unitName,
    String communityName,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer populationServed,
    BigDecimal capacityM3PerDay,
    boolean active
) {
}
