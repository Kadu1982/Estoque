package com.austral.estoque.domain.organization;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Country extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Builder.Default
    private boolean active = true;
}
