package com.arief.warehouse.warehouse_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantUpdateRequest {

    @NotBlank
    private String sku;

    private String color;

    private String size;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;
}
