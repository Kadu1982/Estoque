package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "cost_centers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CostCenter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    private String nature;
    @Builder.Default
    private boolean active = true;
}
