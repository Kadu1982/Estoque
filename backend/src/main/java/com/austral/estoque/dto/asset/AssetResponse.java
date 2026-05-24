package com.austral.estoque.dto.asset;

import java.util.UUID;

public record AssetResponse(
    UUID id,
    String tag,
    String name,
    String type,
    UUID unitId,
    String unitName,
    UUID sectorId,
    String sectorName,
    UUID mainCostCenterId,
    String mainCostCenterCode,
    boolean active
) {
}
