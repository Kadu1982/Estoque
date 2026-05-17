package com.austral.estoque.domain.item;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "item_families")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ItemFamily extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(unique = true, length = 50)
    private String code;
    @Builder.Default
    private boolean active = true;
}
