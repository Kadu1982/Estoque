package com.austral.estoque.domain.item;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "item_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ItemCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private ItemFamily family;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String code;

    @Builder.Default
    private boolean active = true;
}
