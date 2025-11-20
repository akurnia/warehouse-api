package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.ItemCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemResponse;
import com.arief.warehouse.warehouse_api.dto.ItemUpdateRequest;
import com.arief.warehouse.warehouse_api.exception.GlobalExceptionHandler;
import com.arief.warehouse.warehouse_api.exception.NotFoundException;
import com.arief.warehouse.warehouse_api.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@Import(GlobalExceptionHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturn201() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("Item A", "Desc", true);
        ItemResponse response = ItemResponse.builder()
                .id(1L)
                .name("Item A")
                .description("Desc")
                .active(true)
                .build();

        when(itemService.create(any(ItemCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item A"));
    }

    @Test
    void getItem_shouldReturn200_whenFound() throws Exception {
        ItemResponse response = ItemResponse.builder()
                .id(1L)
                .name("Item A")
                .description("Desc")
                .active(true)
                .build();

        when(itemService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getItem_shouldReturn404_whenNotFound() throws Exception {
        when(itemService.getById(1L)).thenThrow(new NotFoundException("Item not found: 1"));

        mockMvc.perform(get("/api/items/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void getAllItems_shouldReturnList() throws Exception {
        ItemResponse r1 = ItemResponse.builder().id(1L).name("A").active(true).build();
        ItemResponse r2 = ItemResponse.builder().id(2L).name("B").active(false).build();

        when(itemService.getAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateItem_shouldReturn200() throws Exception {
        ItemUpdateRequest request = new ItemUpdateRequest("New", "New desc", false);
        ItemResponse response = ItemResponse.builder()
                .id(1L)
                .name("New")
                .description("New desc")
                .active(false)
                .build();

        when(itemService.update(eq(1L), any(ItemUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void deleteItem_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
