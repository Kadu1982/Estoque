package com.austral.estoque.dto.dashboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DashboardSummaryResponse(
    long activeItems,
    long nonDeliveredOrders,
    long pendingRequisitions,
    long activeSuppliers,
    List<OrderStatusCount> orderStatusCounts,
    List<LatestRequisition> latestRequisitions
) {
    public record OrderStatusCount(String status, long count) {}

    public record LatestRequisition(
        UUID id,
        String code,
        String urgency,
        String status,
        String requesterName
    ) {}
}
