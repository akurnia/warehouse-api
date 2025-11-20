package com.arief.warehouse.warehouse_api.service;

public interface ItemVariantService {
    void sell(Long variantId, int quantity);
    void adjustStock(Long variantId, int quantityChange, String requestReason);

}
