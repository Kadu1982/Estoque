package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.OperationalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OperationalUnitRepository extends JpaRepository<OperationalUnit, UUID> {
    Optional<OperationalUnit> findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc();
}
