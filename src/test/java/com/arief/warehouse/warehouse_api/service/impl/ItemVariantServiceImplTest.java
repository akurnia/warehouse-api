package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.entity.Item;
import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import com.arief.warehouse.warehouse_api.entity.StockMovementType;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.ItemRepository;
import com.arief.warehouse.warehouse_api.repository.ItemVariantRepository;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ItemVariantServiceImplTest {

    private ItemRepository itemRepository;
    private ItemVariantRepository itemVariantRepository;
    private StockMovementRepository stockMovementRepository;
    private ItemVariantServiceImpl itemVariantService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemVariantRepository = mock(ItemVariantRepository.class);
        stockMovementRepository = mock(StockMovementRepository.class);

        itemVariantService = new ItemVariantServiceImpl(
                itemRepository,
                itemVariantRepository,
                stockMovementRepository
        );
    }

    private ItemVariant buildVariant(int stockQuantity) {
        Item item = new Item();
        item.setId(1L);
        item.setName("T-Shirt");

        ItemVariant variant = new ItemVariant();
        variant.setId(1L);
        variant.setItem(item);
        variant.setSku("TSHIRT-BLACK-M");
        variant.setColor("Black");
        variant.setSize("M");
        variant.setPrice(new BigDecimal("99000"));
        variant.setStockQuantity(stockQuantity);
        return variant;
    }

    @Test
    void sell_shouldReduceStock_andCreateOutMovement_whenEnoughStock() {
        Long variantId = 1L;
        ItemVariant variant = buildVariant(10);

        when(itemVariantRepository.findByIdForUpdate(variantId))
                .thenReturn(Optional.of(variant));

        itemVariantService.sell(variantId, 3);

        // stok berkurang & disave
        assertThat(variant.getStockQuantity()).isEqualTo(7);
        verify(itemVariantRepository).save(variant);

        // movement tercatat
        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement movement = movementCaptor.getValue();
        assertThat(movement.getVariant()).isEqualTo(variant);
        assertThat(movement.getType()).isEqualTo(StockMovementType.OUT);
        assertThat(movement.getQuantityChange()).isEqualTo(-3);
        assertThat(movement.getReason()).isEqualTo("SALE");
    }

    @Test
    void sell_shouldThrowOutOfStockException_andNotCreateMovement_whenNotEnoughStock() {
        Long variantId = 1L;
        ItemVariant variant = buildVariant(2);

        when(itemVariantRepository.findByIdForUpdate(variantId))
                .thenReturn(Optional.of(variant));

        OutOfStockException ex = assertThrows(
                OutOfStockException.class,
                () -> itemVariantService.sell(variantId, 5)
        );

        assertThat(ex.getVariantId()).isEqualTo(variantId);
        assertThat(ex.getRequested()).isEqualTo(5);
        assertThat(ex.getAvailable()).isEqualTo(2);

        // Tidak ada perubahan stok maupun movement
        verify(itemVariantRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_shouldIncreaseStock_andCreateAdjustmentMovement() {
        Long variantId = 1L;
        ItemVariant variant = buildVariant(5);

        when(itemVariantRepository.findByIdForUpdate(variantId))
                .thenReturn(Optional.of(variant));

        itemVariantService.adjustStock(variantId, 10, "PURCHASE_ORDER_RECEIPT");

        assertThat(variant.getStockQuantity()).isEqualTo(15);
        verify(itemVariantRepository).save(variant);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement movement = movementCaptor.getValue();
        assertThat(movement.getVariant()).isEqualTo(variant);
        assertThat(movement.getType()).isEqualTo(StockMovementType.ADJUSTMENT);
        assertThat(movement.getQuantityChange()).isEqualTo(10);
        assertThat(movement.getReason()).isEqualTo("PURCHASE_ORDER_RECEIPT");
    }

    @Test
    void adjustStock_shouldThrowOutOfStock_whenResultWouldBeNegative() {
        Long variantId = 1L;
        ItemVariant variant = buildVariant(3);

        when(itemVariantRepository.findByIdForUpdate(variantId))
                .thenReturn(Optional.of(variant));

        // mencoba mengurangi stok hingga minus
        OutOfStockException ex = assertThrows(
                OutOfStockException.class,
                () -> itemVariantService.adjustStock(variantId, -5, "CORRECTION")
        );

        assertThat(ex.getVariantId()).isEqualTo(variantId);
        assertThat(ex.getRequested()).isEqualTo(5); // -quantityChange
        assertThat(ex.getAvailable()).isEqualTo(3);

        // stok & movement tidak berubah/disimpan
        assertThat(variant.getStockQuantity()).isEqualTo(3);
        verify(itemVariantRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }
}
