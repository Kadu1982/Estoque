package com.austral.estoque.controller;

import com.austral.estoque.dto.stock.StockBalanceResponse;
import com.austral.estoque.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockBalanceResponse> list(@RequestParam(required = false) Boolean lowStock) {
        return stockService.list(lowStock);
    }

    @GetMapping("/low-stock")
    public List<StockBalanceResponse> lowStock() {
        return stockService.list(true);
    }
}
