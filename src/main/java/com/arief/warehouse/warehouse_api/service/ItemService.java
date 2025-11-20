package com.arief.warehouse.warehouse_api.service;

import com.arief.warehouse.warehouse_api.dto.ItemCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemResponse;
import com.arief.warehouse.warehouse_api.dto.ItemUpdateRequest;

import java.util.List;

public interface ItemService {
    ItemResponse create(ItemCreateRequest request);
    ItemResponse update(Long id, ItemUpdateRequest request);
    ItemResponse getById(Long id);
    List<ItemResponse> getAll();
    void delete(Long id);

}
