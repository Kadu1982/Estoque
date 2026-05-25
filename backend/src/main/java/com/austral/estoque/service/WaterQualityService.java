package com.austral.estoque.service;

import com.austral.estoque.domain.water.WaterSample;
import com.austral.estoque.domain.water.WaterSamplingPoint;
import com.austral.estoque.domain.water.WaterWell;
import com.austral.estoque.dto.water.WaterSampleRequest;
import com.austral.estoque.dto.water.WaterSampleResponse;
import com.austral.estoque.dto.water.WaterSamplingPointRequest;
import com.austral.estoque.dto.water.WaterSamplingPointResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.water.WaterSampleRepository;
import com.austral.estoque.repository.water.WaterSamplingPointRepository;
import com.austral.estoque.repository.water.WaterWellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaterQualityService {

    private final WaterWellRepository waterWellRepository;
    private final WaterSamplingPointRepository samplingPointRepository;
    private final WaterSampleRepository sampleRepository;

    @Transactional
    public WaterSamplingPointResponse createSamplingPoint(WaterSamplingPointRequest request) {
        WaterWell well = waterWellRepository.findByIdAndDeletedAtIsNull(request.wellId())
            .orElseThrow(() -> new ResourceNotFoundException("WaterWell", request.wellId()));
        WaterSamplingPoint point = WaterSamplingPoint.builder()
            .well(well)
            .name(request.name())
            .parameters(request.parameters())
            .frequencyDays(request.frequencyDays())
            .active(true)
            .build();
        return toPointResponse(samplingPointRepository.save(point));
    }

    @Transactional
    public WaterSampleResponse createSample(WaterSampleRequest request) {
        WaterSamplingPoint point = samplingPointRepository.findById(request.samplingPointId())
            .orElseThrow(() -> new ResourceNotFoundException("WaterSamplingPoint", request.samplingPointId()));
        WaterSample sample = WaterSample.builder()
            .samplingPoint(point)
            .sampleCode(request.sampleCode())
            .classification(request.classification())
            .notes(request.notes())
            .build();
        return toSampleResponse(sampleRepository.save(sample));
    }

    private WaterSamplingPointResponse toPointResponse(WaterSamplingPoint point) {
        return new WaterSamplingPointResponse(
            point.getId(),
            point.getWell().getId(),
            point.getWell().getName(),
            point.getName(),
            point.getParameters(),
            point.getFrequencyDays()
        );
    }

    private WaterSampleResponse toSampleResponse(WaterSample sample) {
        return new WaterSampleResponse(
            sample.getId(),
            sample.getSamplingPoint().getId(),
            sample.getSampleCode(),
            sample.getClassification().name(),
            sample.getCollectedAt(),
            sample.getNotes()
        );
    }
}
