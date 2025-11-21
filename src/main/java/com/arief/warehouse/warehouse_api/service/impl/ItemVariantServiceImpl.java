package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.dto.ItemVariantCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemVariantResponse;
import com.arief.warehouse.warehouse_api.dto.ItemVariantUpdateRequest;
import com.arief.warehouse.warehouse_api.entity.Item;
import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import com.arief.warehouse.warehouse_api.entity.StockMovementType;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.ItemRepository;
import com.arief.warehouse.warehouse_api.repository.ItemVariantRepository;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemVariantServiceImpl implements ItemVariantService {

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final StockMovementRepository stockMovementRepository;

    public ItemVariantServiceImpl(ItemRepository itemRepository,
                                  ItemVariantRepository itemVariantRepository,
                                  StockMovementRepository stockMovementRepository) {
        this.itemRepository = itemRepository;
        this.itemVariantRepository = itemVariantRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    // CRUD

    @Override
    public ItemVariantResponse createVariant(Long itemId, ItemVariantCreateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        ItemVariant variant = new ItemVariant();
        variant.setItem(item);
        variant.setSku(request.getSku());
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getInitialStock() == null ? 0 : request.getInitialStock());

        ItemVariant saved = itemVariantRepository.save(variant);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemVariantResponse getVariant(Long variantId) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));
        return toResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemVariantResponse> getVariantsByItem(Long itemId) {
        return itemVariantRepository.findByItemId(itemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ItemVariantResponse updateVariant(Long variantId, ItemVariantUpdateRequest request) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));

        variant.setSku(request.getSku());
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());

        ItemVariant saved = itemVariantRepository.save(variant);
        return toResponse(saved);
    }

    @Override
    public void deleteVariant(Long variantId) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ItemVariant not found: " + variantId));
        itemVariantRepository.delete(variant);
    }

    @Override
    public void sell(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        // Ambil variant dengan DB lock
        ItemVariant variant = itemVariantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() ->
                        new NotFoundException("ItemVariant not found with id: " + variantId)
                );

        int current = variant.getStockQuantity();
        if (current < quantity) {
            // Minta lebih banyak dari stok tersedia
            throw new OutOfStockException(variantId, quantity, current);
        }

        // Update stok
        variant.setStockQuantity(current - quantity);
        itemVariantRepository.save(variant);

        // Catat movement
        StockMovement movement = new StockMovement();
        movement.setVariant(variant);
        movement.setType(StockMovementType.OUT);
        movement.setQuantityChange(-quantity);
        movement.setReason("SALE");
        stockMovementRepository.save(movement);
    }

    @Override
    public void adjustStock(Long variantId, int quantityChange, String reason) {
        if (quantityChange == 0) {
            // Tidak ada perubahan, langsung return
            return;
        }

        ItemVariant variant = itemVariantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() ->
                        new NotFoundException("ItemVariant not found with id: " + variantId)
                );

        int current = variant.getStockQuantity();
        int newQty = current + quantityChange;

        // Jangan ada stok minus
        if (newQty < 0) {
            throw new OutOfStockException(variantId, -quantityChange, current);
        }

        variant.setStockQuantity(newQty);
        itemVariantRepository.save(variant);

        StockMovement movement = new StockMovement();
        movement.setVariant(variant);
        movement.setType(StockMovementType.ADJUSTMENT);
        movement.setQuantityChange(quantityChange);
        movement.setReason(reason);
        stockMovementRepository.save(movement);
    }

    private ItemVariantResponse toResponse(ItemVariant variant) {
        return ItemVariantResponse.builder()
                .id(variant.getId())
                .itemId(variant.getItem().getId())
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }
}
