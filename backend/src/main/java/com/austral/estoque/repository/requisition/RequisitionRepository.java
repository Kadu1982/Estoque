package com.austral.estoque.repository.requisition;

import com.austral.estoque.domain.requisition.Requisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface RequisitionRepository extends JpaRepository<Requisition, UUID> {
    boolean existsByCode(String code);

    @Query("""
        SELECT r FROM Requisition r
        WHERE r.deletedAt IS NULL
        AND (:status IS NULL OR CAST(r.status AS string) = :status)
        AND (:unitId IS NULL OR r.unit.id = :unitId)
        AND (:requesterId IS NULL OR r.requester.id = :requesterId)
        ORDER BY r.createdAt DESC
        """)
    Page<Requisition> search(@Param("status") String status,
                              @Param("unitId") UUID unitId,
                              @Param("requesterId") UUID requesterId,
                              Pageable pageable);

    long countByStatus(Requisition.RequisitionStatus status);
}
