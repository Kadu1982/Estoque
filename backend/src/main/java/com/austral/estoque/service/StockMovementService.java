package com.austral.estoque.service;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.domain.organization.CostCenter;
import com.austral.estoque.domain.organization.Sector;
import com.austral.estoque.domain.organization.Warehouse;
import com.austral.estoque.domain.stock.StockBalance;
import com.austral.estoque.domain.stock.StockMovement;
import com.austral.estoque.domain.user.User;
import com.austral.estoque.dto.stock.StockIssueRequest;
import com.austral.estoque.dto.stock.StockTransferRequest;
import com.austral.estoque.exception.BusinessException;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.item.ItemRepository;
import com.austral.estoque.repository.organization.CostCenterRepository;
import com.austral.estoque.repository.organization.SectorRepository;
import com.austral.estoque.repository.organization.WarehouseRepository;
import com.austral.estoque.repository.stock.StockBalanceRepository;
import com.austral.estoque.repository.stock.StockMovementRepository;
import com.austral.estoque.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementService {

    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final CostCenterRepository costCenterRepository;
    private final StockAlertService stockAlertService;

    @Transactional
    public void transfer(StockTransferRequest request) {
        if (request.sourceWarehouseId().equals(request.targetWarehouseId())) {
            throw new BusinessException("Source and target warehouses must be different");
        }

        Warehouse source = warehouseRepository.findById(request.sourceWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.sourceWarehouseId()));
        Warehouse target = warehouseRepository.findById(request.targetWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.targetWarehouseId()));
        CostCenter costCenter = resolveCostCenter(request.costCenterId());
        User user = currentUser();

        for (var itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemRequest.itemId()));
            StockBalance sourceBalance = balanceOrThrow(source.getId(), item.getId());
            ensureSufficientStock(sourceBalance, itemRequest.quantity());

            sourceBalance.removeStock(itemRequest.quantity());
            stockBalanceRepository.save(sourceBalance);
            stockAlertService.evaluateBalance(sourceBalance);

            StockBalance targetBalance = stockBalanceRepository
                .findByWarehouseIdAndItemId(target.getId(), item.getId())
                .orElseGet(() -> StockBalance.builder()
                    .warehouse(target)
                    .item(item)
                    .quantity(BigDecimal.ZERO)
                    .reservedQuantity(BigDecimal.ZERO)
                    .build());
            targetBalance.addStock(itemRequest.quantity(), sourceBalance.getAvgCostUsd());
            stockBalanceRepository.save(targetBalance);
            stockAlertService.evaluateBalance(targetBalance);

            saveMovement(source, item, StockMovement.MovementType.TRANSFERENCIA_SAIDA, itemRequest.quantity(),
                sourceBalance.getAvgCostUsd(), costCenter, null, "TRANSFER", target.getId(), source, target, user);
            saveMovement(target, item, StockMovement.MovementType.TRANSFERENCIA_ENTRADA, itemRequest.quantity(),
                sourceBalance.getAvgCostUsd(), costCenter, null, "TRANSFER", source.getId(), source, target, user);
        }
    }

    @Transactional
    public void issue(StockIssueRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));
        Sector sector = resolveSector(request.sectorId());
        CostCenter costCenter = resolveCostCenter(request.costCenterId());
        User user = currentUser();

        for (var itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemRequest.itemId()));
            StockBalance balance = balanceOrThrow(warehouse.getId(), item.getId());
            ensureSufficientStock(balance, itemRequest.quantity());

            balance.removeStock(itemRequest.quantity());
            stockBalanceRepository.save(balance);
            stockAlertService.evaluateBalance(balance);

            saveMovement(warehouse, item, StockMovement.MovementType.BAIXA_CONSUMO, itemRequest.quantity(),
                balance.getAvgCostUsd(), costCenter, sector, "ISSUE", request.requestId(), null, null, user);
        }
    }

    private StockBalance balanceOrThrow(UUID warehouseId, UUID itemId) {
        return stockBalanceRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
            .orElseThrow(() -> new BusinessException("Insufficient stock for item " + itemId));
    }

    private void ensureSufficientStock(StockBalance balance, BigDecimal quantity) {
        if (balance.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient stock for item " + balance.getItem().getId());
        }
    }

    private Sector resolveSector(UUID sectorId) {
        if (sectorId == null) {
            return null;
        }
        return sectorRepository.findById(sectorId)
            .orElseThrow(() -> new ResourceNotFoundException("Sector", sectorId));
    }

    private CostCenter resolveCostCenter(UUID costCenterId) {
        if (costCenterId == null) {
            return null;
        }
        return costCenterRepository.findById(costCenterId)
            .orElseThrow(() -> new ResourceNotFoundException("CostCenter", costCenterId));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private void saveMovement(
        Warehouse warehouse,
        Item item,
        StockMovement.MovementType movementType,
        BigDecimal quantity,
        BigDecimal unitCost,
        CostCenter costCenter,
        Sector sector,
        String referenceType,
        UUID referenceId,
        Warehouse origin,
        Warehouse destination,
        User user
    ) {
        BigDecimal totalCost = unitCost == null ? null : unitCost.multiply(quantity);
        stockMovementRepository.save(StockMovement.builder()
            .warehouse(warehouse)
            .item(item)
            .movementType(movementType)
            .quantity(quantity)
            .unitCostUsd(unitCost)
            .totalCostUsd(totalCost)
            .sector(sector)
            .costCenter(costCenter)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .originWarehouse(origin)
            .destinationWarehouse(destination)
            .movementReason(movementType.name())
            .user(user)
            .movementDate(Instant.now())
            .createdAt(Instant.now())
            .build());
    }
}
