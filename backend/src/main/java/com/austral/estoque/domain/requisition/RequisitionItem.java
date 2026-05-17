package com.austral.estoque.domain.requisition;

import com.austral.estoque.domain.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "requisition_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RequisitionItem {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private Requisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "estimated_unit_price_usd", precision = 15, scale = 2)
    private BigDecimal estimatedUnitPriceUsd;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(length = 50)
    private String status = "PENDENTE";

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
