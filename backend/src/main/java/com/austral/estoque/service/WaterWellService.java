package com.austral.estoque.service;

import com.austral.estoque.domain.organization.OperationalUnit;
import com.austral.estoque.domain.water.WaterWell;
import com.austral.estoque.dto.water.WaterWellRequest;
import com.austral.estoque.dto.water.WaterWellResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.organization.OperationalUnitRepository;
import com.austral.estoque.repository.water.WaterWellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaterWellService {

    private final WaterWellRepository waterWellRepository;
    private final OperationalUnitRepository operationalUnitRepository;

    public List<WaterWellResponse> list() {
        return waterWellRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public WaterWellResponse get(UUID id) {
        return waterWellRepository.findByIdAndDeletedAtIsNull(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("WaterWell", id));
    }

    @Transactional
    public WaterWellResponse create(WaterWellRequest request) {
        WaterWell well = WaterWell.builder()
            .name(request.name())
            .unit(resolveUnit(request.unitId()))
            .communityName(request.communityName())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .populationServed(request.populationServed())
            .capacityM3PerDay(request.capacityM3PerDay())
            .active(request.active() == null || request.active())
            .build();
        return toResponse(waterWellRepository.save(well));
    }

    @Transactional
    public WaterWellResponse update(UUID id, WaterWellRequest request) {
        WaterWell well = waterWellRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("WaterWell", id));
        well.setName(request.name());
        well.setUnit(resolveUnit(request.unitId()));
        well.setCommunityName(request.communityName());
        well.setLatitude(request.latitude());
        well.setLongitude(request.longitude());
        well.setPopulationServed(request.populationServed());
        well.setCapacityM3PerDay(request.capacityM3PerDay());
        well.setActive(request.active() == null || request.active());
        return toResponse(waterWellRepository.save(well));
    }

    @Transactional
    public void delete(UUID id) {
        WaterWell well = waterWellRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("WaterWell", id));
        well.softDelete();
        waterWellRepository.save(well);
    }

    private OperationalUnit resolveUnit(UUID unitId) {
        return operationalUnitRepository.findById(unitId)
            .orElseThrow(() -> new ResourceNotFoundException("OperationalUnit", unitId));
    }

    private WaterWellResponse toResponse(WaterWell well) {
        return new WaterWellResponse(
            well.getId(),
            well.getName(),
            well.getUnit().getId(),
            well.getUnit().getName(),
            well.getCommunityName(),
            well.getLatitude(),
            well.getLongitude(),
            well.getPopulationServed(),
            well.getCapacityM3PerDay(),
            well.isActive()
        );
    }
}
