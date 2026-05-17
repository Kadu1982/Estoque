package com.austral.estoque.domain.requisition;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.organization.CostCenter;
import com.austral.estoque.domain.organization.OperationalUnit;
import com.austral.estoque.domain.organization.Sector;
import com.austral.estoque.domain.organization.Warehouse;
import com.austral.estoque.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "requisitions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Requisition extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private OperationalUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private CostCenter costCenter;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequisitionStatus status = RequisitionStatus.RASCUNHO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency = Urgency.NORMAL;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "estimated_total_usd", precision = 15, scale = 2)
    private BigDecimal estimatedTotalUsd;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisitionItem> items = new ArrayList<>();

    public enum RequisitionStatus {
        RASCUNHO, PENDENTE_APROVACAO, APROVADA, REPROVADA,
        EM_COTACAO, PEDIDO_EMITIDO, PARCIALMENTE_RECEBIDA, ENCERRADA, CANCELADA
    }

    public enum Urgency { NORMAL, URGENTE, EMERGENCIAL }
}
