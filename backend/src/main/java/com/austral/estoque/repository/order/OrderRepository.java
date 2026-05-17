package com.austral.estoque.repository.order;

import com.austral.estoque.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByCode(String code);

    Optional<Order> findByCode(String code);

    @Query("""
        SELECT o FROM Order o
        WHERE o.deletedAt IS NULL
        AND (:status IS NULL OR CAST(o.status AS string) = :status)
        AND (:supplierId IS NULL OR o.supplier.id = :supplierId)
        AND (:requesterId IS NULL OR o.requester.id = :requesterId)
        ORDER BY o.createdAt DESC
        """)
    Page<Order> search(@Param("status") String status,
                       @Param("supplierId") UUID supplierId,
                       @Param("requesterId") UUID requesterId,
                       Pageable pageable);

    long countByStatus(Order.OrderStatus status);
    long countByStatusNotAndDeletedAtIsNull(Order.OrderStatus status);
}
