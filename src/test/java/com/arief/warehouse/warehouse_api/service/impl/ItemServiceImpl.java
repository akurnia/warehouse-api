package com.arief.warehouse.warehouse_api.service.impl;

import com.arief.warehouse.warehouse_api.dto.ItemCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemResponse;
import com.arief.warehouse.warehouse_api.dto.ItemUpdateRequest;
import com.arief.warehouse.warehouse_api.entity.Item;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemService = new ItemServiceImpl(itemRepository);
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        ItemCreateRequest request = new ItemCreateRequest("Item A", "Desc", true);

        Item saved = new Item();
        saved.setId(1L);
        saved.setName("Item A");
        saved.setDescription("Desc");
        saved.setActive(true);

        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemResponse response = itemService.create(request);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());

        Item toSave = captor.getValue();
        assertThat(toSave.getName()).isEqualTo("Item A");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Item A");
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Item A");
        item.setDescription("Desc");
        item.setActive(true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Item A");
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnListOfItems() {
        Item i1 = new Item();
        i1.setId(1L);
        i1.setName("Item A");
        i1.setActive(true);

        Item i2 = new Item();
        i2.setId(2L);
        i2.setName("Item B");
        i2.setActive(false);

        when(itemRepository.findAll()).thenReturn(List.of(i1, i2));

        List<ItemResponse> result = itemService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Item A");
    }

    @Test
    void update_shouldUpdateExistingItem() {
        Item existing = new Item();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDescription("Old desc");
        existing.setActive(true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemUpdateRequest request = new ItemUpdateRequest("New", "New desc", false);

        ItemResponse response = itemService.update(1L, request);

        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.isActive()).isFalse();
    }

    @Test
    void delete_shouldCallRepositoryDelete_whenFound() {
        Item existing = new Item();
        existing.setId(1L);
        existing.setName("X");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));

        itemService.delete(1L);

        verify(itemRepository).delete(existing);
    }
}
