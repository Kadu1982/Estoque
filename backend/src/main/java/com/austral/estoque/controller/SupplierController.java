package com.austral.estoque.controller;

import com.austral.estoque.dto.supplier.SupplierRequest;
import com.austral.estoque.dto.supplier.SupplierResponse;
import com.austral.estoque.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public Page<SupplierResponse> list(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return supplierService.list(search, pageable);
    }

    @GetMapping("/{id}")
    public SupplierResponse getById(@PathVariable UUID id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRADOR')")
    public ResponseEntity<SupplierResponse> create(@RequestBody SupplierRequest supplier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRADOR')")
    public SupplierResponse update(@PathVariable UUID id, @RequestBody SupplierRequest body) {
        return supplierService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
