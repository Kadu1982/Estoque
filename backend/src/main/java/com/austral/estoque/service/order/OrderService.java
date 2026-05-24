package com.austral.estoque.service.order;

import com.austral.estoque.domain.order.Order;
import com.austral.estoque.domain.order.OrderItem;
import com.austral.estoque.domain.organization.Warehouse;
import com.austral.estoque.domain.requisition.Requisition;
import com.austral.estoque.domain.stock.StockBalance;
import com.austral.estoque.domain.stock.StockMovement;
import com.austral.estoque.domain.supplier.Supplier;
import com.austral.estoque.domain.user.User;
import com.austral.estoque.dto.order.OrderRequest;
import com.austral.estoque.dto.order.OrderReceiptRequest;
import com.austral.estoque.dto.order.OrderResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.item.ItemRepository;
import com.austral.estoque.repository.order.OrderItemRepository;
import com.austral.estoque.repository.organization.WarehouseRepository;
import com.austral.estoque.repository.order.OrderRepository;
import com.austral.estoque.repository.requisition.RequisitionRepository;
import com.austral.estoque.repository.stock.StockBalanceRepository;
import com.austral.estoque.repository.stock.StockMovementRepository;
import com.austral.estoque.repository.supplier.SupplierRepository;
import com.austral.estoque.repository.user.UserRepository;
import com.austral.estoque.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final RequisitionRepository requisitionRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockAlertService stockAlertService;

    public Optional<OrderResponse> findById(UUID id) {
        return orderRepository.findById(id).map(this::toResponse);
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Optional<OrderResponse> findByCode(String code) {
        return orderRepository.findByCode(code).map(this::toResponse);
    }

    public long countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        User requester = resolveRequester(request.requesterId());
        Requisition requisition = resolveRequisition(request.requisitionId());

        Order order = Order.builder()
            .code(request.code())
            .supplier(supplier)
            .requester(requester)
            .requisition(requisition)
            .notes(request.notes())
            .status(Order.OrderStatus.ABERTO)
            .items(new ArrayList<>())
            .build();

        applyItems(order, request);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(UUID id, OrderRequest request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        order.setCode(request.code());
        order.setSupplier(
            supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()))
        );
        order.setRequester(resolveRequester(request.requesterId()));
        order.setRequisition(resolveRequisition(request.requisitionId()));
        order.setNotes(request.notes());

        order.getItems().clear();
        applyItems(order, request);

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void delete(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        order.softDelete();
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse receiveOrder(UUID orderId, OrderReceiptRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));
        User receiver = userRepository.findById(request.receiverId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.receiverId()));

        for (var itemReceipt : request.items()) {
            OrderItem orderItem = orderItemRepository.findById(itemReceipt.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemReceipt.orderItemId()));
            if (!orderItem.getOrder().getId().equals(order.getId())) {
                throw new IllegalArgumentException("Order item does not belong to this order");
            }

            StockBalance balance = stockBalanceRepository
                .findByWarehouseIdAndItemId(warehouse.getId(), orderItem.getItem().getId())
                .orElseGet(() -> StockBalance.builder()
                    .warehouse(warehouse)
                    .item(orderItem.getItem())
                    .quantity(BigDecimal.ZERO)
                    .reservedQuantity(BigDecimal.ZERO)
                    .build());

            balance.addStock(itemReceipt.quantity(), orderItem.getUnitPriceUsd());
            stockBalanceRepository.save(balance);
            stockAlertService.evaluateBalance(balance);

            orderItem.setReceivedQuantity(orderItem.getReceivedQuantity().add(itemReceipt.quantity()));
            orderItemRepository.save(orderItem);

            StockMovement movement = StockMovement.builder()
                .warehouse(warehouse)
                .item(orderItem.getItem())
                .movementType(StockMovement.MovementType.ENTRADA)
                .quantity(itemReceipt.quantity())
                .unitCostUsd(orderItem.getUnitPriceUsd())
                .totalCostUsd(orderItem.getUnitPriceUsd().multiply(itemReceipt.quantity()))
                .referenceType("PURCHASE_ORDER")
                .referenceId(order.getId())
                .movementReason("ORDER_RECEIPT")
                .user(receiver)
                .movementDate(Instant.now())
                .createdAt(Instant.now())
                .build();
            stockMovementRepository.save(movement);
        }

        boolean fullyReceived = order.getItems().stream()
            .allMatch(i -> i.getReceivedQuantity().compareTo(i.getQuantity()) >= 0);
        order.setStatus(fullyReceived ? Order.OrderStatus.ENTREGUE : Order.OrderStatus.PARCIALMENTE_RECEBIDO);
        return toResponse(orderRepository.save(order));
    }

    private User resolveRequester(UUID requesterId) {
        if (requesterId == null) {
            return userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("User", "admin"));
        }
        return userRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));
    }

    private Requisition resolveRequisition(UUID requisitionId) {
        if (requisitionId == null) {
            return null;
        }
        return requisitionRepository.findById(requisitionId)
            .orElseThrow(() -> new ResourceNotFoundException("Requisition", requisitionId));
    }

    private void applyItems(Order order, OrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            return;
        }
        for (var itemRequest : request.items()) {
            var item = itemRepository.findById(itemRequest.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemRequest.itemId()));
            BigDecimal total = itemRequest.quantity().multiply(itemRequest.unitPrice());
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(itemRequest.quantity())
                .unitPriceUsd(itemRequest.unitPrice())
                .totalPriceUsd(total)
                .receivedQuantity(BigDecimal.ZERO)
                .build();
            order.getItems().add(orderItem);
        }
    }

    private OrderResponse toResponse(Order order) {
        var supplier = new OrderResponse.SupplierSummary(order.getSupplier().getId(), order.getSupplier().getName());
        var requester = new OrderResponse.RequesterSummary(
            order.getRequester().getId(),
            order.getRequester().getUsername(),
            order.getRequester().getFullName()
        );
        var items = order.getItems().stream()
            .map(i -> new OrderResponse.OrderItemResponse(
                i.getId(),
                i.getItem().getId(),
                i.getItem().getCode(),
                i.getItem().getDescription(),
                i.getQuantity(),
                i.getReceivedQuantity(),
                i.getUnitPriceUsd(),
                i.getTotalPriceUsd()
            ))
            .toList();
        return new OrderResponse(order.getId(), order.getCode(), order.getStatus().name(), order.getNotes(), supplier, requester, items);
    }
}
