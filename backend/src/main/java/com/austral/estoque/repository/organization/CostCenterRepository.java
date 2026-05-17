package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {
    Optional<CostCenter> findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc();
}
