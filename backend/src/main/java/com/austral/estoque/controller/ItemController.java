package com.austral.estoque.controller;

import com.austral.estoque.dto.item.ItemRequest;
import com.austral.estoque.dto.item.ItemResponse;
import com.austral.estoque.service.ItemService;
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
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Page<ItemResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) Boolean active,
        @PageableDefault(size = 20, sort = "description") Pageable pageable
    ) {
        return itemService.list(search, categoryId, active, pageable);
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable UUID id) {
        return itemService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE')")
    public ResponseEntity<ItemResponse> create(@RequestBody ItemRequest item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMOXARIFE')")
    public ItemResponse update(@PathVariable UUID id, @RequestBody ItemRequest body) {
        return itemService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
