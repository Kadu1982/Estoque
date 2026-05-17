package com.austral.estoque.repository.stock;

import com.austral.estoque.domain.stock.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository extends JpaRepository<StockBalance, UUID> {
    Optional<StockBalance> findByWarehouseIdAndItemId(UUID warehouseId, UUID itemId);

    @Query("SELECT sb FROM StockBalance sb WHERE sb.item.id = :itemId")
    List<StockBalance> findAllByItemId(@Param("itemId") UUID itemId);

    @Query("SELECT sb FROM StockBalance sb WHERE sb.warehouse.id = :warehouseId AND sb.quantity > 0")
    List<StockBalance> findStockByWarehouse(@Param("warehouseId") UUID warehouseId);

    @Query("""
        SELECT sb FROM StockBalance sb
        WHERE sb.item.minStock IS NOT NULL
        AND sb.item.minStock > 0
        AND sb.quantity < sb.item.minStock
        """)
    List<StockBalance> findBelowMinimum();
}
