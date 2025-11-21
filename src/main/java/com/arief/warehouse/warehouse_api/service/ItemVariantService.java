package com.arief.warehouse.warehouse_api.service;

import com.arief.warehouse.warehouse_api.dto.ItemVariantCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemVariantResponse;
import com.arief.warehouse.warehouse_api.dto.ItemVariantUpdateRequest;

import java.util.List;

public interface ItemVariantService {

    // CRUD Variant
    ItemVariantResponse createVariant(Long itemId, ItemVariantCreateRequest request);

    ItemVariantResponse getVariant(Long variantId);

    List<ItemVariantResponse> getVariantsByItem(Long itemId);

    ItemVariantResponse updateVariant(Long variantId, ItemVariantUpdateRequest request);

    void deleteVariant(Long variantId);

    void sell(Long variantId, int quantity);

    void adjustStock(Long variantId, int quantityChange, String reason);
}
