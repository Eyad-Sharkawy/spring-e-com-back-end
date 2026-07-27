package dev.eyadsharkawy.spring_e_com.dtos.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for adding a product to a cart")
public record CartItemRequest(
        @Schema(description = "UUID of the product to add to the cart", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotBlank String productId,

        @Schema(description = "Quantity of the product to add", example = "2")
        @Positive int quantity
) {
}
