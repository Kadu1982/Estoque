package com.austral.estoque.domain.asset;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.organization.CostCenter;
import com.austral.estoque.domain.organization.OperationalUnit;
import com.austral.estoque.domain.organization.Sector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String tag;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssetType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private OperationalUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_cost_center_id")
    private CostCenter mainCostCenter;

    @Builder.Default
    private boolean active = true;

    public enum AssetType {
        EQUIPAMENTO_SAUDE,
        MAQUINA,
        VEICULO,
        OUTRO
    }
}
