package com.austral.estoque.service;

import com.austral.estoque.domain.requisition.Requisition;
import com.austral.estoque.domain.user.User;
import com.austral.estoque.dto.requisition.RequisitionRequest;
import com.austral.estoque.dto.requisition.RequisitionResponse;
import com.austral.estoque.exception.BusinessException;
import com.austral.estoque.repository.organization.CostCenterRepository;
import com.austral.estoque.repository.organization.OperationalUnitRepository;
import com.austral.estoque.repository.organization.WarehouseRepository;
import com.austral.estoque.repository.requisition.RequisitionRepository;
import com.austral.estoque.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final UserRepository userRepository;
    private final OperationalUnitRepository operationalUnitRepository;
    private final CostCenterRepository costCenterRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public Page<RequisitionResponse> list(Requisition.RequisitionStatus status, Pageable pageable) {
        return requisitionRepository.search(
            status != null ? status.name() : null,
            null,
            null,
            pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RequisitionResponse findById(UUID id) {
        return requisitionRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException("Requisicao nao encontrada"));
    }

    @Transactional
    public RequisitionResponse create(RequisitionRequest request) {
        User requester = userRepository.findByUsername("admin")
            .orElseThrow(() -> new BusinessException("Usuario admin nao encontrado"));
        var unit = operationalUnitRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow(() -> new BusinessException("Unidade operacional nao encontrada"));
        var costCenter = costCenterRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow(() -> new BusinessException("Centro de custo nao encontrado"));
        var warehouse = warehouseRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc().orElse(null);

        Requisition requisition = Requisition.builder()
            .code("REQ-" + Instant.now().toEpochMilli())
            .requester(requester)
            .unit(unit)
            .costCenter(costCenter)
            .warehouse(warehouse)
            .urgency(request.urgency() != null ? request.urgency() : Requisition.Urgency.NORMAL)
            .justification(request.justification())
            .notes(request.notes())
            .build();
        requisition.setStatus(Requisition.RequisitionStatus.RASCUNHO);
        return toResponse(requisitionRepository.save(requisition));
    }

    @Transactional
    public RequisitionResponse update(UUID id, RequisitionRequest body) {
        Requisition existing = requisitionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Requisicao nao encontrada"));
        existing.setNotes(body.notes());
        if (body.urgency() != null) {
            existing.setUrgency(body.urgency());
        }
        if (body.justification() != null) {
            existing.setJustification(body.justification());
        }
        return toResponse(requisitionRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        Requisition existing = requisitionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Requisicao nao encontrada"));
        requisitionRepository.delete(existing);
    }

    @Transactional
    public RequisitionResponse approve(UUID id) {
        Requisition existing = requisitionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Requisicao nao encontrada"));
        existing.setStatus(Requisition.RequisitionStatus.APROVADA);
        return toResponse(requisitionRepository.save(existing));
    }

    @Transactional
    public RequisitionResponse reject(UUID id) {
        Requisition existing = requisitionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Requisicao nao encontrada"));
        existing.setStatus(Requisition.RequisitionStatus.REPROVADA);
        return toResponse(requisitionRepository.save(existing));
    }

    private RequisitionResponse toResponse(Requisition requisition) {
        RequisitionResponse.Requester requester = requisition.getRequester() == null
            ? null
            : new RequisitionResponse.Requester(requisition.getRequester().getId(), requisition.getRequester().getFullName());
        var items = requisition.getItems() == null ? java.util.List.<RequisitionResponse.RequisitionItemResponse>of()
            : requisition.getItems().stream()
                .map(i -> new RequisitionResponse.RequisitionItemResponse(
                    i.getId(),
                    i.getItem() != null ? i.getItem().getId() : null,
                    i.getItem() != null ? i.getItem().getCode() : null,
                    i.getItem() != null ? i.getItem().getDescription() : null,
                    i.getQuantity(),
                    i.getEstimatedUnitPriceUsd(),
                    i.getEstimatedUnitPriceUsd() != null && i.getQuantity() != null
                        ? i.getEstimatedUnitPriceUsd().multiply(i.getQuantity())
                        : null
                ))
                .toList();
        return new RequisitionResponse(
            requisition.getId(),
            requisition.getCode(),
            requisition.getStatus() != null ? requisition.getStatus().name() : null,
            requisition.getUrgency() != null ? requisition.getUrgency().name() : null,
            requisition.getJustification(),
            requisition.getEstimatedTotalUsd(),
            requester,
            items
        );
    }
}
