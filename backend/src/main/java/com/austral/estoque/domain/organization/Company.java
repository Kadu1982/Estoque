package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Company extends BaseEntity {
    @Column(nullable = false)
    private String name;
    private String document;
    @Builder.Default
    private boolean active = true;
}
