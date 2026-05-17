package com.austral.estoque.service;

import com.austral.estoque.domain.supplier.Supplier;
import com.austral.estoque.dto.supplier.SupplierRequest;
import com.austral.estoque.dto.supplier.SupplierResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Page<SupplierResponse> list(String search, Pageable pageable) {
        return supplierRepository.search(search, pageable).map(this::toResponse);
    }

    public SupplierResponse findById(UUID id) {
        return supplierRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
            .code(request.code())
            .name(request.name())
            .type(request.type())
            .country(request.country())
            .currency(request.currency())
            .paymentTermDays(request.paymentTermDays())
            .contactName(request.contactName())
            .contactEmail(request.contactEmail())
            .contactPhone(request.contactPhone())
            .contactWhatsapp(request.contactWhatsapp())
            .notes(request.notes())
            .active(request.active() == null || request.active())
            .build();
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest body) {
        Supplier s = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
        s.setName(body.name());
        s.setType(body.type());
        s.setCountry(body.country());
        s.setCurrency(body.currency());
        s.setPaymentTermDays(body.paymentTermDays());
        s.setContactName(body.contactName());
        s.setContactEmail(body.contactEmail());
        s.setContactPhone(body.contactPhone());
        s.setContactWhatsapp(body.contactWhatsapp());
        s.setNotes(body.notes());
        if (body.active() != null) {
            s.setActive(body.active());
        }
        return toResponse(supplierRepository.save(s));
    }

    @Transactional
    public void delete(UUID id) {
        Supplier s = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
        s.softDelete();
        supplierRepository.save(s);
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(
            s.getId(),
            s.getCode(),
            s.getName(),
            s.getCountry(),
            s.getCurrency(),
            s.getPaymentTermDays(),
            s.getContactName(),
            s.getContactEmail(),
            s.getContactPhone(),
            s.isActive()
        );
    }
}
