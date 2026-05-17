package com.austral.estoque.controller;

import com.austral.estoque.dto.common.LookupOptionResponse;
import com.austral.estoque.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/warehouses")
    public List<LookupOptionResponse> warehouses() {
        return lookupService.warehouses();
    }

    @GetMapping("/users")
    public List<LookupOptionResponse> users() {
        return lookupService.users();
    }
}
