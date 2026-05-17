package com.austral.estoque.domain.stock;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.domain.organization.Warehouse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "stock_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "item_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockBalance {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "avg_cost_usd", precision = 15, scale = 2)
    private BigDecimal avgCostUsd;

    @Column(name = "last_movement_at")
    private Instant lastMovementAt;

    @Builder.Default
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public boolean isBelowMinimum() {
        BigDecimal minStock = item.getMinStock();
        return minStock != null && quantity.compareTo(minStock) < 0;
    }

    public void addStock(BigDecimal qty, BigDecimal unitCost) {
        if (avgCostUsd == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            avgCostUsd = unitCost;
        } else if (unitCost != null) {
            // Custo médio ponderado
            BigDecimal totalCost = quantity.multiply(avgCostUsd).add(qty.multiply(unitCost));
            BigDecimal newQty = quantity.add(qty);
            avgCostUsd = totalCost.divide(newQty, 2, java.math.RoundingMode.HALF_UP);
        }
        quantity = quantity.add(qty);
        lastMovementAt = Instant.now();
        updatedAt = Instant.now();
    }

    public void removeStock(BigDecimal qty) {
        quantity = quantity.subtract(qty);
        lastMovementAt = Instant.now();
        updatedAt = Instant.now();
    }
}
