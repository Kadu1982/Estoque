package com.austral.estoque.controller;

import com.austral.estoque.domain.requisition.Requisition;
import com.austral.estoque.dto.requisition.RequisitionRequest;
import com.austral.estoque.dto.requisition.RequisitionResponse;
import com.austral.estoque.service.RequisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requisitions")
@RequiredArgsConstructor
public class RequisitionController {

    private final RequisitionService requisitionService;

    @GetMapping
    public Page<RequisitionResponse> list(
        @RequestParam(required = false) Requisition.RequisitionStatus status,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return requisitionService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public RequisitionResponse getById(@PathVariable UUID id) {
        return requisitionService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE', 'SOLICITANTE')")
    public ResponseEntity<RequisitionResponse> create(@RequestBody RequisitionRequest requisition) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requisitionService.create(requisition));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE', 'SOLICITANTE')")
    public RequisitionResponse update(@PathVariable UUID id, @RequestBody RequisitionRequest body) {
        return requisitionService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        requisitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_SETOR', 'FINANCEIRO', 'DIRETOR')")
    public RequisitionResponse approve(@PathVariable UUID id) {
        return requisitionService.approve(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_SETOR', 'FINANCEIRO', 'DIRETOR')")
    public RequisitionResponse reject(@PathVariable UUID id) {
        return requisitionService.reject(id);
    }
}
