package com.austral.estoque.repository.water;

import com.austral.estoque.domain.water.WaterSamplingPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaterSamplingPointRepository extends JpaRepository<WaterSamplingPoint, UUID> {
}
