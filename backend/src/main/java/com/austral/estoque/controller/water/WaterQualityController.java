package com.austral.estoque.controller.water;

import com.austral.estoque.dto.water.WaterSampleRequest;
import com.austral.estoque.dto.water.WaterSampleResponse;
import com.austral.estoque.dto.water.WaterSamplingPointRequest;
import com.austral.estoque.dto.water.WaterSamplingPointResponse;
import com.austral.estoque.service.WaterQualityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
public class WaterQualityController {

    private final WaterQualityService waterQualityService;

    @PostMapping("/sampling-points")
    @ResponseStatus(HttpStatus.CREATED)
    public WaterSamplingPointResponse createSamplingPoint(@Valid @RequestBody WaterSamplingPointRequest request) {
        return waterQualityService.createSamplingPoint(request);
    }

    @PostMapping("/samples")
    @ResponseStatus(HttpStatus.CREATED)
    public WaterSampleResponse createSample(@Valid @RequestBody WaterSampleRequest request) {
        return waterQualityService.createSample(request);
    }
}
