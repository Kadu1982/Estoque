package com.austral.estoque.service;

import com.austral.estoque.domain.order.Order;
import com.austral.estoque.domain.requisition.Requisition;
import com.austral.estoque.dto.dashboard.DashboardSummaryResponse;
import com.austral.estoque.repository.item.ItemRepository;
import com.austral.estoque.repository.order.OrderRepository;
import com.austral.estoque.repository.requisition.RequisitionRepository;
import com.austral.estoque.repository.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final RequisitionRepository requisitionRepository;
    private final SupplierRepository supplierRepository;

    public DashboardSummaryResponse summary() {
        long activeItems = itemRepository.countByActiveTrueAndDeletedAtIsNull();
        long nonDeliveredOrders = orderRepository.countByStatusNotAndDeletedAtIsNull(Order.OrderStatus.ENTREGUE);
        long pendingRequisitions = requisitionRepository.countByStatus(Requisition.RequisitionStatus.PENDENTE_APROVACAO);
        long activeSuppliers = supplierRepository.countByActiveTrueAndDeletedAtIsNull();

        List<DashboardSummaryResponse.OrderStatusCount> orderStatusCounts = Arrays.stream(Order.OrderStatus.values())
            .map(status -> new DashboardSummaryResponse.OrderStatusCount(status.name(), orderRepository.countByStatus(status)))
            .toList();

        var latestPage = requisitionRepository.search(
            null,
            null,
            null,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        List<DashboardSummaryResponse.LatestRequisition> latestRequisitions = latestPage.getContent().stream()
            .map(r -> new DashboardSummaryResponse.LatestRequisition(
                r.getId(),
                r.getCode(),
                r.getUrgency().name(),
                r.getStatus().name(),
                r.getRequester() != null ? r.getRequester().getFullName() : "-"
            ))
            .toList();

        return new DashboardSummaryResponse(
            activeItems,
            nonDeliveredOrders,
            pendingRequisitions,
            activeSuppliers,
            orderStatusCounts,
            latestRequisitions
        );
    }
}
