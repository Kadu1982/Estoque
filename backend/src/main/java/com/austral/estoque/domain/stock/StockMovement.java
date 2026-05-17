package com.austral.estoque.domain.stock;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.domain.organization.*;
import com.austral.estoque.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "stock_movements")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockMovement {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost_usd", precision = 15, scale = 2)
    private BigDecimal unitCostUsd;

    @Column(name = "total_cost_usd", precision = 15, scale = 2)
    private BigDecimal totalCostUsd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_warehouse_id")
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id")
    private Warehouse destinationWarehouse;

    @Column(name = "movement_reason", length = 255)
    private String movementReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "movement_date", nullable = false)
    private Instant movementDate = Instant.now();

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public enum MovementType {
        ENTRADA, SAIDA, TRANSFERENCIA_SAIDA, TRANSFERENCIA_ENTRADA,
        AJUSTE_POSITIVO, AJUSTE_NEGATIVO, DEVOLUCAO, RESERVA, BAIXA_CONSUMO
    }
}
