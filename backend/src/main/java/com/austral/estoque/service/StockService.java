package com.austral.estoque.service;

import com.austral.estoque.dto.stock.StockBalanceResponse;
import com.austral.estoque.repository.stock.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockAlertService stockAlertService;
    private final StockStatusCalculator stockStatusCalculator;

    public List<StockBalanceResponse> list(Boolean lowStock) {
        var data = Boolean.TRUE.equals(lowStock)
            ? stockBalanceRepository.findBelowMinimum()
            : stockBalanceRepository.findAll();
        return data.stream().map(this::toResponse).toList();
    }

    private StockBalanceResponse toResponse(com.austral.estoque.domain.stock.StockBalance sb) {
        var plannedQuantity = stockAlertService.plannedQuantityFor(sb);
        var percentage = stockStatusCalculator.percentageOfPlanned(sb.getQuantity(), plannedQuantity);
        var statusColor = stockStatusCalculator.statusFor(percentage);
        return new StockBalanceResponse(
            sb.getId(),
            sb.getItem().getId(),
            sb.getItem().getCode(),
            sb.getItem().getDescription(),
            sb.getWarehouse().getId(),
            sb.getWarehouse().getName(),
            sb.getQuantity(),
            sb.getItem().getMinStock(),
            sb.getItem().getMaxStock(),
            sb.getItem().getUnitOfMeasure(),
            plannedQuantity,
            percentage,
            statusColor.name()
        );
    }
}
