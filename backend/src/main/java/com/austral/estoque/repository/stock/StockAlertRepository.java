package com.austral.estoque.repository.stock;

import com.austral.estoque.domain.stock.StockAlert;
import com.austral.estoque.domain.stock.StockAlertStatus;
import com.austral.estoque.domain.stock.StockStatusColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {
    List<StockAlert> findAllByOrderByCreatedAtDesc();

    List<StockAlert> findByStatusColorOrderByCreatedAtDesc(StockStatusColor statusColor);

    Optional<StockAlert> findFirstByWarehouseIdAndItemIdAndStatusColorAndAlertStatusOrderByCreatedAtDesc(
        UUID warehouseId,
        UUID itemId,
        StockStatusColor statusColor,
        StockAlertStatus alertStatus
    );
}
