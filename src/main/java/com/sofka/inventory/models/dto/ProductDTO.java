package com.sofka.inventory.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String id;

    @NotNull
    private String code;

    @NotEmpty
    private String description;

    private Boolean enabled;

    @Min(value = 0)
    private Integer quantity;
}
