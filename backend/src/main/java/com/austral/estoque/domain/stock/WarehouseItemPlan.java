package com.austral.estoque.domain.stock;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.item.Item;
import com.austral.estoque.domain.organization.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_item_plans",
    uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "item_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseItemPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "planned_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQuantity;

    @Column(name = "min_level", precision = 15, scale = 3)
    private BigDecimal minLevel;

    @Column(name = "safety_level", precision = 15, scale = 3)
    private BigDecimal safetyLevel;

    @Builder.Default
    private boolean active = true;
}
