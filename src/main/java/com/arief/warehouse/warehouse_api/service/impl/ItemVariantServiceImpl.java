package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.ItemVariantRepository;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemVariantServiceImpl implements ItemVariantService {

    private final ItemVariantRepository itemVariantRepository;

    public ItemVariantServiceImpl(ItemVariantRepository itemVariantRepository) {
        this.itemVariantRepository = itemVariantRepository;
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
    }

    @Override
    public void adjustStock(Long variantId, int quantityChange) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));

        int newQty = variant.getStockQuantity() + quantityChange;
        if (newQty < 0) {
            throw new OutOfStockException(variantId, -quantityChange, variant.getStockQuantity());
        }

        variant.setStockQuantity(newQty);
        itemVariantRepository.save(variant);
    }
}
