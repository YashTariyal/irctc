package com.irctc.train.controller;

import com.irctc.train.dto.PriceAlertRequest;
import com.irctc.train.dto.PriceAlertResponse;
import com.irctc.train.dto.PriceAlertUpdateRequest;
import com.irctc.train.service.PriceAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains/price-alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    @PostMapping
    public ResponseEntity<PriceAlertResponse> createAlert(@Valid @RequestBody PriceAlertRequest request) {
        return ResponseEntity.ok(priceAlertService.createAlert(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceAlertResponse> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(priceAlertService.getAlert(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PriceAlertResponse>> getAlertsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(priceAlertService.getAlertsForUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceAlertResponse> updateAlert(@PathVariable Long id,
                                                          @Valid @RequestBody PriceAlertUpdateRequest request) {
        return ResponseEntity.ok(priceAlertService.updateAlert(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        priceAlertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}

