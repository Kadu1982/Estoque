package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operational_units")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OperationalUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UnitType type;

    private String address;
    @Builder.Default
    private boolean active = true;

    public enum UnitType {
        HOSPITAL, CANTEIRO, OFICINA, ADMINISTRATIVO, BASE_REGIONAL
    }
}
