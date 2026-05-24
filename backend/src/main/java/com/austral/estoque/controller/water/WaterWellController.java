package com.austral.estoque.controller.water;

import com.austral.estoque.dto.water.WaterWellRequest;
import com.austral.estoque.dto.water.WaterWellResponse;
import com.austral.estoque.service.WaterWellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/water/wells")
@RequiredArgsConstructor
public class WaterWellController {

    private final WaterWellService waterWellService;

    @GetMapping
    public List<WaterWellResponse> list() {
        return waterWellService.list();
    }

    @GetMapping("/{id}")
    public WaterWellResponse get(@PathVariable UUID id) {
        return waterWellService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaterWellResponse create(@Valid @RequestBody WaterWellRequest request) {
        return waterWellService.create(request);
    }

    @PutMapping("/{id}")
    public WaterWellResponse update(@PathVariable UUID id, @Valid @RequestBody WaterWellRequest request) {
        return waterWellService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        waterWellService.delete(id);
    }
}
