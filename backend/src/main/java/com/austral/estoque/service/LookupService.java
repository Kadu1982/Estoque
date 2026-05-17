package com.austral.estoque.service;

import com.austral.estoque.dto.common.LookupOptionResponse;
import com.austral.estoque.repository.organization.WarehouseRepository;
import com.austral.estoque.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LookupService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    public List<LookupOptionResponse> warehouses() {
        return warehouseRepository.findAll().stream()
            .filter(w -> w.getDeletedAt() == null && w.isActive())
            .map(w -> new LookupOptionResponse(w.getId(), w.getName()))
            .toList();
    }

    public List<LookupOptionResponse> users() {
        return userRepository.findAll().stream()
            .filter(u -> u.getDeletedAt() == null && u.isActive())
            .map(u -> new LookupOptionResponse(u.getId(), u.getFullName()))
            .toList();
    }
}
