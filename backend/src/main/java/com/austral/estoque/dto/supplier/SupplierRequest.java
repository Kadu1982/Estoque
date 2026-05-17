package com.austral.estoque.dto.supplier;

public record SupplierRequest(
    String code,
    String name,
    String type,
    String country,
    String currency,
    Integer paymentTermDays,
    String contactName,
    String contactEmail,
    String contactPhone,
    String contactWhatsapp,
    String notes,
    Boolean active
) {
}
