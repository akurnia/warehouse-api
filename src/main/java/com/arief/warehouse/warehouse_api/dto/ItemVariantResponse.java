package com.arief.warehouse.warehouse_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantResponse {

    private Long id;
    private Long itemId;
    private String sku;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer stockQuantity;
}
