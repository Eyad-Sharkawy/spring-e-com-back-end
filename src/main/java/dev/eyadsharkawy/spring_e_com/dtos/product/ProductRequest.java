package dev.eyadsharkawy.spring_e_com.dtos.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Schema(description = "Request body for creating or updating a product")
public record ProductRequest(
        @Schema(description = "Seller or manufacturer of the product", example = "Apple")
        @NotBlank String seller,

        @Schema(description = "Display name of the product", example = "iPhone 15 Pro")
        @NotBlank String name,

        @Schema(description = "Detailed description of the product features", example = "Titanium design, A17 Pro chip, Action button.")
        String description,

        @Schema(description = "Unit price of the product in USD", example = "999.99")
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,

        @Schema(description = "Available quantity in stock", example = "50")
        @Min(0) int stock
) {
}