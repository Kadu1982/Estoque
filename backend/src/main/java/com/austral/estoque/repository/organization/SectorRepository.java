package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {
    Optional<Sector> findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc();
}
