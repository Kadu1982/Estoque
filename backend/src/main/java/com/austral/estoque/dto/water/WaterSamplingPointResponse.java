package com.austral.estoque.dto.water;

import java.util.UUID;

public record WaterSamplingPointResponse(
    UUID id,
    UUID wellId,
    String wellName,
    String name,
    String parameters,
    Integer frequencyDays
) {
}
