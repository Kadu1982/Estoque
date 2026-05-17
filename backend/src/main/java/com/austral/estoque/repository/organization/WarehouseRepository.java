package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    java.util.Optional<Warehouse> findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc();
}
