package com.austral.estoque.service;

import com.austral.estoque.domain.item.Item;
import com.austral.estoque.dto.item.ItemRequest;
import com.austral.estoque.dto.item.ItemResponse;
import com.austral.estoque.exception.ResourceNotFoundException;
import com.austral.estoque.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public Page<ItemResponse> list(String search, UUID categoryId, Boolean active, Pageable pageable) {
        return itemRepository.search(search, categoryId, active, pageable).map(this::toResponse);
    }

    public ItemResponse findById(UUID id) {
        return itemRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        Item item = Item.builder()
            .code(request.code())
            .description(request.description())
            .unitOfMeasure(request.unitOfMeasure())
            .brand(request.brand())
            .specification(request.specification())
            .criticality(request.criticality() != null ? request.criticality() : Item.Criticality.MEDIO)
            .minStock(request.minStock())
            .maxStock(request.maxStock())
            .leadTimeDays(request.leadTimeDays())
            .active(request.active() == null || request.active())
            .build();
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(UUID id, ItemRequest body) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        item.setDescription(body.description());
        item.setBrand(body.brand());
        item.setSpecification(body.specification());
        item.setCriticality(body.criticality());
        item.setMinStock(body.minStock());
        item.setMaxStock(body.maxStock());
        item.setLeadTimeDays(body.leadTimeDays());
        item.setUnitOfMeasure(body.unitOfMeasure());
        if (body.active() != null) {
            item.setActive(body.active());
        }
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        item.softDelete();
        itemRepository.save(item);
    }

    private ItemResponse toResponse(Item item) {
        ItemResponse.CategoryResponse category = item.getCategory() == null
            ? null
            : new ItemResponse.CategoryResponse(item.getCategory().getId(), item.getCategory().getName());
        return new ItemResponse(
            item.getId(),
            item.getCode(),
            item.getDescription(),
            item.getBrand(),
            item.getUnitOfMeasure(),
            item.getCriticality() != null ? item.getCriticality().name() : null,
            item.getMinStock(),
            item.getMaxStock(),
            item.getLeadTimeDays(),
            item.isActive(),
            category
        );
    }
}
