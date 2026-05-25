package com.austral.estoque.dto.water;

import java.time.Instant;
import java.util.UUID;

public record WaterSampleResponse(
    UUID id,
    UUID samplingPointId,
    String sampleCode,
    String classification,
    Instant collectedAt,
    String notes
) {
}
