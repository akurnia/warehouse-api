package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.ItemCreateRequest;
import com.arief.warehouse.warehouse_api.dto.ItemUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItem_thenGetItem_returns201And200WithJson() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("T-Shirt", "Basic cotton T-Shirt", true);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/items/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("T-Shirt"))
                .andExpect(jsonPath("$.description").value("Basic cotton T-Shirt"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getItem_notFound_returns404WithErrorJson() throws Exception {
        mockMvc.perform(get("/api/items/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Item not found")))
                .andExpect(jsonPath("$.path").value("/api/items/9999"));
    }

    @Test
    void updateItem_thenDeleteItem_flowWorksWithProperStatusCodes() throws Exception {
        // 1) Create item
        ItemCreateRequest create = new ItemCreateRequest("Shoes", "Running shoes", true);
        String createJson = objectMapper.writeValueAsString(create);

        String responseBody = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(responseBody).get("id").asLong();

        ItemUpdateRequest update = new ItemUpdateRequest("Shoes Pro", "Running shoes updated", false);
        String updateJson = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Shoes Pro"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/items/{id}", id))
                .andExpect(status().isNoContent());
    }
}
