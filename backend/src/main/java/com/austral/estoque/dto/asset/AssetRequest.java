package com.austral.estoque.dto.asset;

import com.austral.estoque.domain.asset.Asset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssetRequest(
    @NotBlank String tag,
    @NotBlank String name,
    @NotNull Asset.AssetType type,
    @NotNull UUID unitId,
    UUID sectorId,
    UUID mainCostCenterId,
    Boolean active
) {
}
