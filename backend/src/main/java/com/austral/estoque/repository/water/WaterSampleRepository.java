package com.austral.estoque.repository.water;

import com.austral.estoque.domain.water.WaterSample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaterSampleRepository extends JpaRepository<WaterSample, UUID> {
}
