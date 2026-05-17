package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "sectors")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Sector extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private OperationalUnit unit;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String code;

    @Builder.Default
    private boolean active = true;
}
