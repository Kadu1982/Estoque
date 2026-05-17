package com.austral.estoque.domain.order;

import com.austral.estoque.domain.common.BaseEntity;
import com.austral.estoque.domain.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "purchase_order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price_usd", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPriceUsd;

    @Column(name = "total_price_usd", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPriceUsd;

    @Builder.Default
    @Column(name = "received_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;
}
