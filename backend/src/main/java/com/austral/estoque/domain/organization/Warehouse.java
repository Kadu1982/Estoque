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

    private String description;
    @Builder.Default
    private boolean active = true;
}
