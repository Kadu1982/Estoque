package com.austral.estoque.dto.water;

import com.austral.estoque.domain.water.WaterSample;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WaterSampleRequest(
    @NotNull UUID samplingPointId,
    @NotBlank String sampleCode,
    @NotNull WaterSample.SampleClassification classification,
    String notes
) {
}
