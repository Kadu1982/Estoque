package com.austral.estoque.service;

import com.austral.estoque.domain.stock.StockStatusColor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class StockStatusCalculator {

    public BigDecimal percentageOfPlanned(BigDecimal quantity, BigDecimal plannedQuantity) {
        if (quantity == null || plannedQuantity == null || plannedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return quantity
            .multiply(BigDecimal.valueOf(100))
            .divide(plannedQuantity, 2, RoundingMode.HALF_UP);
    }

    public StockStatusColor statusFor(BigDecimal percentageOfPlanned) {
        if (percentageOfPlanned == null) {
            return StockStatusColor.GREEN;
        }
        if (percentageOfPlanned.compareTo(BigDecimal.valueOf(30)) <= 0) {
            return StockStatusColor.RED;
        }
        if (percentageOfPlanned.compareTo(BigDecimal.valueOf(60)) <= 0) {
            return StockStatusColor.YELLOW;
        }
        return StockStatusColor.GREEN;
    }
}
