package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import com.arief.warehouse.warehouse_api.entity.StockMovementType;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.ItemVariantRepository;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemVariantServiceImplTest {

    private ItemVariantRepository itemVariantRepository;
    private StockMovementRepository stockMovementRepository;
    private ItemVariantServiceImpl itemVariantService;

    @BeforeEach
    void setUp() {
        itemVariantRepository = mock(ItemVariantRepository.class);
        stockMovementRepository = mock(StockMovementRepository.class);
        itemVariantService = new ItemVariantServiceImpl(itemVariantRepository, stockMovementRepository);
    }

    @Test
    void sell_shouldReduceStock_andCreateOutMovement_whenEnoughStock() {
        Long variantId = 1L;
        ItemVariant variant = new ItemVariant();
        variant.setId(variantId);
        variant.setSku("SKU-1");
        variant.setPrice(BigDecimal.valueOf(100_000));
        variant.setStockQuantity(10);

        when(itemVariantRepository.findById(variantId))
                .thenReturn(Optional.of(variant));

        itemVariantService.sell(variantId, 3);

        // verify stock updated & saved
        ArgumentCaptor<ItemVariant> captor = ArgumentCaptor.forClass(ItemVariant.class);
        verify(itemVariantRepository).save(captor.capture());
        ItemVariant saved = captor.getValue();
        assertEquals(7, saved.getStockQuantity());

        // verify stock movement recorded
        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement movement = movementCaptor.getValue();

        assertEquals(StockMovementType.OUT, movement.getType());
        assertEquals(-3, movement.getQuantityChange());
        assertSame(variant, movement.getVariant());
    }

    @Test
    void sell_shouldThrowOutOfStockException_andNotCreateMovement_whenNotEnoughStock() {
        Long variantId = 1L;
        ItemVariant variant = new ItemVariant();
        variant.setId(variantId);
        variant.setSku("SKU-1");
        variant.setPrice(BigDecimal.valueOf(100_000));
        variant.setStockQuantity(2);

        when(itemVariantRepository.findById(variantId))
                .thenReturn(Optional.of(variant));

        assertThrows(OutOfStockException.class,
                () -> itemVariantService.sell(variantId, 5));

        verify(itemVariantRepository, never()).save(any(ItemVariant.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }
}
