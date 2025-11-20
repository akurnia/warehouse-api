package com.arief.warehouse.warehouse_api.controller;

import com.arief.warehouse.warehouse_api.dto.SellRequest;
import com.arief.warehouse.warehouse_api.exception.GlobalExceptionHandler;
import com.arief.warehouse.warehouse_api.exception.OutOfStockException;
import com.arief.warehouse.warehouse_api.repository.StockMovementRepository;
import com.arief.warehouse.warehouse_api.service.ItemVariantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.arief.warehouse.warehouse_api.entity.StockMovement;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(controllers = ItemVariantController.class)
@Import(GlobalExceptionHandler.class)
class ItemVariantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemVariantService itemVariantService;

    @MockitoBean
    private StockMovementRepository stockMovementRepository;

    @Test
    void sell_shouldReturn200_whenSuccess() throws Exception {
        SellRequest request = new SellRequest(3);

        mockMvc.perform(post("/api/variants/{id}/sell", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        Mockito.verify(itemVariantService).sell(1L, 3);
    }

    @Test
    void sell_shouldReturn400_whenOutOfStock() throws Exception {
        SellRequest request = new SellRequest(5);

        Mockito.doThrow(new OutOfStockException(1L, 5, 2))
                .when(itemVariantService).sell(anyLong(), anyInt());

        mockMvc.perform(post("/api/variants/{id}/sell", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("OUT_OF_STOCK"));
    }

    @Test
    void getMovements_shouldReturn200WithList() throws Exception {
        StockMovement m = new StockMovement();
        m.setId(1L);

        Mockito.when(stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(m));

        mockMvc.perform(get("/api/variants/{id}/movements", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
