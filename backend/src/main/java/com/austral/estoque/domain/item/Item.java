package com.austral.estoque.domain.item;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Item extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ItemCategory category;

    @Column(name = "unit_of_measure", nullable = false, length = 50)
    private String unitOfMeasure;

    private String brand;

    @Column(columnDefinition = "TEXT")
    private String specification;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Criticality criticality = Criticality.MEDIO;

    @Builder.Default
    @Column(name = "min_stock", precision = 15, scale = 3)
    private BigDecimal minStock = BigDecimal.ZERO;

    @Column(name = "max_stock", precision = 15, scale = 3)
    private BigDecimal maxStock;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Builder.Default
    private boolean active = true;

    public enum Criticality { CRITICO, ALTO, MEDIO, BAIXO }
}
