package com.austral.estoque.repository.stock;

import com.austral.estoque.domain.stock.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
}
