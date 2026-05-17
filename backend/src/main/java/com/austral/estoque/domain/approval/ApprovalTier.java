package com.austral.estoque.domain.approval;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "approval_tiers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ApprovalTier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "min_amount_usd", nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmountUsd = BigDecimal.ZERO;

    @Column(name = "max_amount_usd", precision = 15, scale = 2)
    private BigDecimal maxAmountUsd; // null = sem limite

    @Column(name = "approver_role", nullable = false, length = 50)
    private String approverRole;

    @Column(name = "item_criticality", length = 20)
    private String itemCriticality; // null = qualquer

    @Column(name = "special_rule", length = 50)
    private String specialRule; // CAPEX, EMERGENCIAL, FORA_CONTRATO

    @Builder.Default
    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours = 24;

    @Builder.Default
    @Column(name = "escalation_after_hours")
    private Integer escalationAfterHours = 48;

    @Builder.Default
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Builder.Default
    private boolean active = true;

    public boolean matches(BigDecimal amount) {
        boolean aboveMin = amount.compareTo(minAmountUsd) >= 0;
        boolean belowMax = maxAmountUsd == null || amount.compareTo(maxAmountUsd) <= 0;
        return aboveMin && belowMax;
    }
}
