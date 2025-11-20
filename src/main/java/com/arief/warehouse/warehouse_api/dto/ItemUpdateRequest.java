package com.arief.warehouse.warehouse_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateRequest {

    @NotBlank
    private String name;
    private String description;
    private Boolean active;
}
