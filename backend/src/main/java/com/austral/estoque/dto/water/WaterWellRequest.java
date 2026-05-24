package com.austral.estoque.dto.water;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WaterWellRequest(
    @NotBlank String name,
    @NotNull UUID unitId,
    @NotBlank String communityName,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer populationServed,
    BigDecimal capacityM3PerDay,
    Boolean active
) {
}
