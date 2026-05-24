package com.austral.estoque.repository.water;

import com.austral.estoque.domain.water.WaterWell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterWellRepository extends JpaRepository<WaterWell, UUID> {
    List<WaterWell> findByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<WaterWell> findByIdAndDeletedAtIsNull(UUID id);
}
