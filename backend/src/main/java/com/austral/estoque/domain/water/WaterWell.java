package com.austral.estoque.domain.water;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.organization.OperationalUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "water_wells")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterWell extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private OperationalUnit unit;

    @Column(name = "community_name", nullable = false)
    private String communityName;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "population_served")
    private Integer populationServed;

    @Column(name = "capacity_m3_per_day", precision = 12, scale = 3)
    private BigDecimal capacityM3PerDay;

    @Builder.Default
    private boolean active = true;
}
