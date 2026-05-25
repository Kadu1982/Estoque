package com.austral.estoque.dto.water;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WaterSamplingPointRequest(
    @NotNull UUID wellId,
    @NotBlank String name,
    String parameters,
    Integer frequencyDays
) {
}
