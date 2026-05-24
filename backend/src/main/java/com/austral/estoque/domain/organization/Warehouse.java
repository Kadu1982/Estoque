package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "warehouses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Warehouse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private OperationalUnit unit;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private WarehouseType type = WarehouseType.DESCENTRALIZADO;

    private String description;
    @Builder.Default
    private boolean active = true;

    public enum WarehouseType {
        CENTRAL,
        DESCENTRALIZADO
    }
}
