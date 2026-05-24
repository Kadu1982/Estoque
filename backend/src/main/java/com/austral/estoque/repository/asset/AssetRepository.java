package com.austral.estoque.repository.asset;

import com.austral.estoque.domain.asset.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Asset> findByIdAndDeletedAtIsNull(UUID id);
}
