package com.austral.estoque.service;

import com.austral.estoque.domain.asset.Asset;
import com.austral.estoque.domain.organization.CostCenter;
import com.austral.estoque.domain.organization.OperationalUnit;
import com.austral.estoque.domain.organization.Sector;
import com.austral.estoque.dto.asset.AssetRequest;
import com.austral.estoque.dto.asset.AssetResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.asset.AssetRepository;
import com.austral.estoque.repository.organization.CostCenterRepository;
import com.austral.estoque.repository.organization.OperationalUnitRepository;
import com.austral.estoque.repository.organization.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final OperationalUnitRepository operationalUnitRepository;
    private final SectorRepository sectorRepository;
    private final CostCenterRepository costCenterRepository;

    public List<AssetResponse> list() {
        return assetRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public AssetResponse get(UUID id) {
        return assetRepository.findByIdAndDeletedAtIsNull(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    @Transactional
    public AssetResponse create(AssetRequest request) {
        Asset asset = Asset.builder()
            .tag(request.tag())
            .name(request.name())
            .type(request.type())
            .unit(resolveUnit(request.unitId()))
            .sector(resolveSector(request.sectorId()))
            .mainCostCenter(resolveCostCenter(request.mainCostCenterId()))
            .active(request.active() == null || request.active())
            .build();
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse update(UUID id, AssetRequest request) {
        Asset asset = assetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        asset.setTag(request.tag());
        asset.setName(request.name());
        asset.setType(request.type());
        asset.setUnit(resolveUnit(request.unitId()));
        asset.setSector(resolveSector(request.sectorId()));
        asset.setMainCostCenter(resolveCostCenter(request.mainCostCenterId()));
        asset.setActive(request.active() == null || request.active());
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void delete(UUID id) {
        Asset asset = assetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        asset.softDelete();
        assetRepository.save(asset);
    }

    private OperationalUnit resolveUnit(UUID unitId) {
        return operationalUnitRepository.findById(unitId)
            .orElseThrow(() -> new ResourceNotFoundException("OperationalUnit", unitId));
    }

    private Sector resolveSector(UUID sectorId) {
        if (sectorId == null) {
            return null;
        }
        return sectorRepository.findById(sectorId)
            .orElseThrow(() -> new ResourceNotFoundException("Sector", sectorId));
    }

    private CostCenter resolveCostCenter(UUID costCenterId) {
        if (costCenterId == null) {
            return null;
        }
        return costCenterRepository.findById(costCenterId)
            .orElseThrow(() -> new ResourceNotFoundException("CostCenter", costCenterId));
    }

    private AssetResponse toResponse(Asset asset) {
        Sector sector = asset.getSector();
        CostCenter costCenter = asset.getMainCostCenter();
        return new AssetResponse(
            asset.getId(),
            asset.getTag(),
            asset.getName(),
            asset.getType().name(),
            asset.getUnit().getId(),
            asset.getUnit().getName(),
            sector == null ? null : sector.getId(),
            sector == null ? null : sector.getName(),
            costCenter == null ? null : costCenter.getId(),
            costCenter == null ? null : costCenter.getCode(),
            asset.isActive()
        );
    }
}
