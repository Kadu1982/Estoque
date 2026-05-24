package com.austral.estoque.repository.stock;

import com.austral.estoque.domain.stock.WarehouseItemPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseItemPlanRepository extends JpaRepository<WarehouseItemPlan, UUID> {
    Optional<WarehouseItemPlan> findByWarehouseIdAndItemIdAndActiveTrue(UUID warehouseId, UUID itemId);
}
