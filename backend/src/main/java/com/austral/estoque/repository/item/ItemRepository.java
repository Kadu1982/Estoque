package com.austral.estoque.repository.item;

import com.austral.estoque.domain.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    Optional<Item> findByCode(String code);
    boolean existsByCode(String code);
    long countByActiveTrueAndDeletedAtIsNull();

    @Query("""
        SELECT i FROM Item i
        WHERE i.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR i.category.id = :categoryId)
        AND (:active IS NULL OR i.active = :active)
        """)
    Page<Item> search(@Param("search") String search,
                      @Param("categoryId") UUID categoryId,
                      @Param("active") Boolean active,
                      Pageable pageable);

    @Query("SELECT i FROM Item i JOIN StockBalance sb ON sb.item = i WHERE sb.quantity < i.minStock AND i.minStock > 0")
    java.util.List<Item> findItemsBelowMinimumStock();
}
