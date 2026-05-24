package com.austral.estoque.service;

import com.austral.estoque.domain.stock.StockAlert;
import com.austral.estoque.domain.stock.StockAlertStatus;
import com.austral.estoque.domain.stock.StockBalance;
import com.austral.estoque.domain.stock.StockStatusColor;
import com.austral.estoque.dto.stock.StockAlertResponse;
import com.austral.estoque.repository.stock.StockAlertRepository;
import com.austral.estoque.repository.stock.WarehouseItemPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final WarehouseItemPlanRepository warehouseItemPlanRepository;
    private final StockStatusCalculator stockStatusCalculator;

    public List<StockAlertResponse> list(StockStatusColor statusColor) {
        var alerts = statusColor == null
            ? stockAlertRepository.findAllByOrderByCreatedAtDesc()
            : stockAlertRepository.findByStatusColorOrderByCreatedAtDesc(statusColor);
        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void evaluateBalance(StockBalance balance) {
        BigDecimal plannedQuantity = plannedQuantityFor(balance);
        BigDecimal percentage = stockStatusCalculator.percentageOfPlanned(balance.getQuantity(), plannedQuantity);
        StockStatusColor statusColor = stockStatusCalculator.statusFor(percentage);

        if (statusColor != StockStatusColor.RED || percentage == null) {
            return;
        }

        var existingOpenRedAlert = stockAlertRepository
            .findFirstByWarehouseIdAndItemIdAndStatusColorAndAlertStatusOrderByCreatedAtDesc(
                balance.getWarehouse().getId(),
                balance.getItem().getId(),
                StockStatusColor.RED,
                StockAlertStatus.OPEN
            );

        if (existingOpenRedAlert.isPresent()) {
            return;
        }

        stockAlertRepository.save(StockAlert.builder()
            .warehouse(balance.getWarehouse())
            .item(balance.getItem())
            .alertType("LOW_STOCK")
            .statusColor(StockStatusColor.RED)
            .alertStatus(StockAlertStatus.OPEN)
            .quantityOnHand(balance.getQuantity())
            .plannedQuantity(plannedQuantity)
            .percentageOfPlanned(percentage)
            .build());
    }

    public BigDecimal plannedQuantityFor(StockBalance balance) {
        Optional<BigDecimal> explicitPlan = warehouseItemPlanRepository
            .findByWarehouseIdAndItemIdAndActiveTrue(balance.getWarehouse().getId(), balance.getItem().getId())
            .map(plan -> plan.getPlannedQuantity());
        return explicitPlan.orElse(balance.getItem().getMaxStock());
    }

    private StockAlertResponse toResponse(StockAlert alert) {
        return new StockAlertResponse(
            alert.getId(),
            alert.getWarehouse().getId(),
            alert.getWarehouse().getName(),
            alert.getItem().getId(),
            alert.getItem().getCode(),
            alert.getItem().getDescription(),
            alert.getQuantityOnHand(),
            alert.getPlannedQuantity(),
            alert.getPercentageOfPlanned(),
            alert.getStatusColor().name(),
            alert.getAlertType(),
            alert.getAlertStatus().name(),
            alert.getCreatedAt()
        );
    }
}
