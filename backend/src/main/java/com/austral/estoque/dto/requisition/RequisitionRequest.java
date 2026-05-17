package com.austral.estoque.dto.requisition;

import com.austral.estoque.domain.requisition.Requisition;

public record RequisitionRequest(
    Requisition.Urgency urgency,
    String justification,
    String notes
) {
}
