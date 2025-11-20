package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.dto.ItemCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemResponse;
import com.arief.warehouse.warehouse_api.dto.ItemUpdateRequest;
import com.arief.warehouse.warehouse_api.entity.Item;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.repository.ItemRepository;
import com.arief.warehouse.warehouse_api.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemResponse create(ItemCreateRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setActive(request.getActive() == null ? true : request.getActive());

        Item saved = itemRepository.save(item);
        return toResponse(saved);
    }

    @Override
    public ItemResponse update(Long id, ItemUpdateRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found: " + id));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }

        Item updated = itemRepository.save(item);
        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found: " + id));
        return toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAll() {
        return itemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found: " + id));
        itemRepository.delete(item);
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .active(item.isActive())
                .build();
    }
}
