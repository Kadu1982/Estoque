package com.austral.estoque.dto.supplier;

import java.util.UUID;

public record SupplierResponse(
    UUID id,
    String code,
    String name,
    String country,
    String currency,
    Integer paymentTermDays,
    String contactName,
    String contactEmail,
    String contactPhone,
    boolean active
) {
}
