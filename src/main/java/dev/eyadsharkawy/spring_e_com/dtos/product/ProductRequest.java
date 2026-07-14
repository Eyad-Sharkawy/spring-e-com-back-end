package dev.eyadsharkawy.spring_e_com.dtos.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String seller,
        @NotBlank String name,
        String description,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @Min(0) int stock
) {
}