package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.ItemVariantUpdateRequest;
import com.arief.warehouse.warehouse_api.dto.SellRequest;
import com.arief.warehouse.warehouse_api.dto.StockAdjustmentRequest;
import com.arief.warehouse.warehouse_api.entity.Item;
import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import com.arief.warehouse.warehouse_api.entity.StockMovementType;
import com.arief.warehouse.warehouse_api.repository.ItemRepository;
import com.arief.warehouse.warehouse_api.repository.ItemVariantRepository;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ItemVariantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemVariantRepository itemVariantRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private ItemVariant createVariantWithStock(int stock) {
        Item item = new Item();
        item.setName("Hoodie");
        item.setDescription("Warehouse test hoodie");
        item.setActive(true);
        item = itemRepository.save(item);

        ItemVariant variant = new ItemVariant();
        variant.setItem(item);
        variant.setSku("HD-001-BLACK-M");
        variant.setColor("Black");
        variant.setSize("M");
        variant.setPrice(BigDecimal.valueOf(250_000));
        variant.setStockQuantity(stock);

        return itemVariantRepository.save(variant);
    }

    @Test
    void variantCrud_flow_create_list_update_delete() throws Exception {
        Item item = new Item();
        item.setName("T-Shirt");
        item.setDescription("Integration test item");
        item.setActive(true);
        item = itemRepository.save(item);
        Long itemId = item.getId();

        String createJson = """
            {
              "sku": "TSHIRT-BLACK-M",
              "color": "Black",
              "size": "M",
              "price": 99000.0,
              "initialStock": 10
            }
            """;

        String createResponse = mockMvc.perform(post("/api/items/{itemId}/variants", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long variantId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/items/{itemId}/variants", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(variantId))
                .andExpect(jsonPath("$[0].sku").value("TSHIRT-BLACK-M"));

        ItemVariantUpdateRequest updateRequest = new ItemVariantUpdateRequest(
                "TSHIRT-BLACK-M-NEW",
                "Black",
                "M",
                BigDecimal.valueOf(105_000)
        );
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/variants/{id}", variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(variantId))
                .andExpect(jsonPath("$.sku").value("TSHIRT-BLACK-M-NEW"))
                .andExpect(jsonPath("$.price").value(105_000.0));

        mockMvc.perform(delete("/api/variants/{id}", variantId))
                .andExpect(status().isNoContent());
    }

    @Test
    void sellVariant_success_reducesStock_andCreatesOutMovement() throws Exception {
        ItemVariant variant = createVariantWithStock(10);
        Long variantId = variant.getId();

        SellRequest request = new SellRequest(3);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/variants/{id}/sell", variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));

        ItemVariant updated = itemVariantRepository.findById(variantId).orElseThrow();
        assertEquals(7, updated.getStockQuantity());

        List<StockMovement> movements =
                stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
        assertEquals(1, movements.size());
        StockMovement movement = movements.get(0);

        assertEquals(StockMovementType.OUT, movement.getType());
        assertEquals(-3, movement.getQuantityChange());
        assertEquals("SALE", movement.getReason());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    void sellVariant_outOfStock_returns400WithErrorJson_andNoMovement() throws Exception {
        ItemVariant variant = createVariantWithStock(2);
        Long variantId = variant.getId();

        SellRequest request = new SellRequest(5);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/variants/{id}/sell", variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$.message", containsString("Not enough stock")))
                .andExpect(jsonPath("$.path").value("/api/variants/" + variantId + "/sell"));

        ItemVariant after = itemVariantRepository.findById(variantId).orElseThrow();
        assertEquals(2, after.getStockQuantity());

        List<StockMovement> movements =
                stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
        assertEquals(0, movements.size());
    }

    @Test
    void sellVariant_variantNotFound_returns404ErrorJson() throws Exception {
        SellRequest request = new SellRequest(1);
        String json = objectMapper.writeValueAsString(request);

        Long nonExistingId = 9999L;

        mockMvc.perform(post("/api/variants/{id}/sell", nonExistingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("ItemVariant not found")))
                .andExpect(jsonPath("$.path").value("/api/variants/" + nonExistingId + "/sell"));
    }

    @Test
    void sellVariant_validationError_missingQuantity_returns400ValidationJson() throws Exception {
        String json = "{}";

        ItemVariant variant = createVariantWithStock(5);

        mockMvc.perform(post("/api/variants/{id}/sell", variant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    void adjustStock_success_createsAdjustmentMovement_andUpdatesStock() throws Exception {
        ItemVariant variant = createVariantWithStock(5);
        Long variantId = variant.getId();

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(10, "PURCHASE_ORDER_RECEIPT");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/variants/{id}/stock/adjust", variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ItemVariant updated = itemVariantRepository.findById(variantId).orElseThrow();
        assertEquals(15, updated.getStockQuantity());

        List<StockMovement> movements =
                stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
        assertEquals(1, movements.size());
        StockMovement movement = movements.get(0);

        assertEquals(StockMovementType.ADJUSTMENT, movement.getType());
        assertEquals(10, movement.getQuantityChange());
        assertEquals("PURCHASE_ORDER_RECEIPT", movement.getReason());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    void getMovements_afterSell_returnsJsonArrayWithCorrectFields() throws Exception {
        ItemVariant variant = createVariantWithStock(8);
        Long variantId = variant.getId();

        SellRequest request = new SellRequest(3);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/variants/{id}/sell", variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/variants/{id}/movements", variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].type").value("OUT"))
                .andExpect(jsonPath("$[0].quantityChange").value(-3))
                .andExpect(jsonPath("$[0].reason").value("SALE"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()));
    }
}
