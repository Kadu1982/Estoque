package com.austral.estoque.domain.water;

import com.austral.estoque.domain.common.BaseEntity;
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

import java.time.Instant;

@Entity
@Table(name = "water_samples")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterSample extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampling_point_id", nullable = false)
    private WaterSamplingPoint samplingPoint;

    @Column(name = "sample_code", nullable = false, unique = true, length = 100)
    private String sampleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SampleClassification classification;

    @Builder.Default
    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum SampleClassification {
        CONFORME,
        NAO_CONFORME,
        PENDENTE
    }
}
