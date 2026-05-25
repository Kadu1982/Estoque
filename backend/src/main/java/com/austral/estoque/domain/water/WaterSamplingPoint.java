package com.austral.estoque.domain.water;

import com.austral.estoque.domain.common.BaseEntity;
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

@Entity
@Table(name = "water_sampling_points")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterSamplingPoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "well_id", nullable = false)
    private WaterWell well;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "frequency_days")
    private Integer frequencyDays;

    @Builder.Default
    private boolean active = true;
}
