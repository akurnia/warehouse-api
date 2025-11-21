package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.ItemVariantCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemVariantResponse;
import com.arief.warehouse.warehouse_api.dto.ItemVariantUpdateRequest;
import com.arief.warehouse.warehouse_api.dto.SellRequest;
import com.arief.warehouse.warehouse_api.dto.StockAdjustmentRequest;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ItemVariantController {

    private final ItemVariantService itemVariantService;
    private final StockMovementRepository stockMovementRepository;

    public ItemVariantController(ItemVariantService itemVariantService,
                                 StockMovementRepository stockMovementRepository) {
        this.itemVariantService = itemVariantService;
        this.stockMovementRepository = stockMovementRepository;
    }

    // CRUD VARIANT

    @PostMapping("/items/{itemId}/variants")
    public ResponseEntity<ItemVariantResponse> createVariant(@PathVariable Long itemId,
                                                             @Valid @RequestBody ItemVariantCreateRequest request) {
        ItemVariantResponse created = itemVariantService.createVariant(itemId, request);
        return ResponseEntity
                .created(URI.create("/api/variants/" + created.getId()))
                .body(created);
    }

    @GetMapping("/items/{itemId}/variants")
    public ResponseEntity<List<ItemVariantResponse>> getVariantsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemVariantService.getVariantsByItem(itemId));
    }

    @GetMapping("/variants/{id}")
    public ResponseEntity<ItemVariantResponse> getVariant(@PathVariable Long id) {
        return ResponseEntity.ok(itemVariantService.getVariant(id));
    }

    @PutMapping("/variants/{id}")
    public ResponseEntity<ItemVariantResponse> updateVariant(@PathVariable Long id,
                                                             @Valid @RequestBody ItemVariantUpdateRequest request) {
        return ResponseEntity.ok(itemVariantService.updateVariant(id, request));
    }

    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        itemVariantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }

    // STOCK OPERATIONS

    @PostMapping("/variants/{id}/sell")
    public ResponseEntity<Void> sell(@PathVariable("id") Long variantId,
                                     @Valid @RequestBody SellRequest request) {
        itemVariantService.sell(variantId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/variants/{id}/stock/adjust")
    public ResponseEntity<Void> adjustStock(@PathVariable("id") Long variantId,
                                            @Valid @RequestBody StockAdjustmentRequest request) {
        itemVariantService.adjustStock(variantId, request.getQuantityChange(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/variants/{id}/movements")
    public ResponseEntity<List<StockMovement>> getMovements(@PathVariable("id") Long variantId) {
        List<StockMovement> movements =
                stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
        return ResponseEntity.ok(movements);
    }
}
