package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.entity.*;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.*;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemVariantServiceImpl implements ItemVariantService {

    private final ItemVariantRepository itemVariantRepository;
    private final StockMovementRepository stockMovementRepository;

    public ItemVariantServiceImpl(ItemVariantRepository itemVariantRepository,
                                  StockMovementRepository stockMovementRepository) {
        this.itemVariantRepository = itemVariantRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public void sell(Long variantId, int quantity) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));

        int current = variant.getStockQuantity();
        if (current < quantity) {
            throw new OutOfStockException(variantId, quantity, current);
        }

        variant.setStockQuantity(current - quantity);
        itemVariantRepository.save(variant);

        StockMovement movement = new StockMovement();
        movement.setVariant(variant);
        movement.setType(StockMovementType.OUT);
        movement.setQuantityChange(-quantity);
        movement.setReason("SALE"); // atau nanti diambil dari parameter kalau mau
        stockMovementRepository.save(movement);
    }

    @Override
    public void adjustStock(Long variantId, int quantityChange, String requestReason) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));

        int newQty = variant.getStockQuantity() + quantityChange;
        if (newQty < 0) {
            throw new OutOfStockException(variantId, -quantityChange, variant.getStockQuantity());
        }

        variant.setStockQuantity(newQty);
        itemVariantRepository.save(variant);

        StockMovement movement = new StockMovement();
        movement.setVariant(variant);
        movement.setType(StockMovementType.ADJUSTMENT);
        movement.setQuantityChange(quantityChange);
        movement.setReason(requestReason);
        stockMovementRepository.save(movement);
    }
}
