package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.SellRequest;
import com.arief.warehouse.warehouse_api.dto.StockAdjustmentRequest;
import com.arief.warehouse.warehouse_api.entity.*;
import com.arief.warehouse.warehouse_api.repository.*;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/variants")
public class ItemVariantController {

    private final ItemVariantService itemVariantService;
    private final StockMovementRepository stockMovementRepository;

    public ItemVariantController(ItemVariantService itemVariantService, StockMovementRepository stockMovementRepository) {
        this.itemVariantService = itemVariantService;
        this.stockMovementRepository = stockMovementRepository;
    }

    @PostMapping("/{id}/sell")
    public ResponseEntity<Void> sell(@PathVariable("id") Long variantId,
                                     @Valid @RequestBody SellRequest request) {
        itemVariantService.sell(variantId, request.getQuantity());
        // test return dulu
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stock/adjust")
    public ResponseEntity<Void> adjustStock(@PathVariable("id") Long variantId,
                                            @Valid @RequestBody StockAdjustmentRequest request) {
        itemVariantService.adjustStock(variantId, request.getQuantityChange(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<List<StockMovement>> getMovements(@PathVariable("id") Long variantId) {
        List<StockMovement> movements = stockMovementRepository
                .findByVariantIdOrderByCreatedAtDesc(variantId);
        return ResponseEntity.ok(movements);
    }
}
