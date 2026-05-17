package com.austral.estoque.domain.approval;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.requisition.Requisition;
import com.austral.estoque.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "approvals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Approval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private Requisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private ApprovalTier tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDENTE;

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    public enum ApprovalStatus { PENDENTE, APROVADA, REPROVADA, ESCALADA }
}
