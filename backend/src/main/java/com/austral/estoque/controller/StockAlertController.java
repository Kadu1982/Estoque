package com.austral.estoque.controller;

import com.austral.estoque.domain.stock.StockStatusColor;
import com.austral.estoque.dto.stock.StockAlertResponse;
import com.austral.estoque.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-alerts")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    @GetMapping
    public List<StockAlertResponse> list(@RequestParam(required = false) StockStatusColor status) {
        return stockAlertService.list(status);
    }
}
