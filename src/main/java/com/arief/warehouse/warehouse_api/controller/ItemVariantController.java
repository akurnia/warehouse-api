package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.SellRequest;
import com.arief.warehouse.warehouse_api.dto.StockAdjustmentRequest;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/variants")
public class ItemVariantController {

    private final ItemVariantService itemVariantService;

    public ItemVariantController(ItemVariantService itemVariantService) {
        this.itemVariantService = itemVariantService;
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
        itemVariantService.adjustStock(variantId, request.getQuantityChange());
        return ResponseEntity.ok().build();
    }
}
