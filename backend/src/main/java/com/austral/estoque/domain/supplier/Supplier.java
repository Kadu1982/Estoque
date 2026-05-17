package com.austral.estoque.domain.supplier;

import com.austral.estoque.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "suppliers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String type; // LOCAL, IMPORTADO

    private String country;

    @Builder.Default
    @Column(length = 10)
    private String currency = "USD";

    @Column(name = "payment_term_days")
    private Integer paymentTermDays;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_whatsapp")
    private String contactWhatsapp;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean active = true;
}
