package com.austral.estoque.repository.approval;

import com.austral.estoque.domain.approval.ApprovalTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ApprovalTierRepository extends JpaRepository<ApprovalTier, UUID> {
    List<ApprovalTier> findByActiveOrderByOrderIndexAsc(boolean active);
}
