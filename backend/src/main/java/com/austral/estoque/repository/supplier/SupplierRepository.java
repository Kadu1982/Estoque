package com.austral.estoque.repository.supplier;

import com.austral.estoque.domain.supplier.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByCode(String code);
    long countByActiveTrueAndDeletedAtIsNull();

    @Query("""
        SELECT s FROM Supplier s WHERE s.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%',:search,'%'))
             OR LOWER(s.code) LIKE LOWER(CONCAT('%',:search,'%')))
        """)
    Page<Supplier> search(@Param("search") String search, Pageable pageable);
}
