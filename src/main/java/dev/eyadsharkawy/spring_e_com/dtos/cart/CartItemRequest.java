package dev.eyadsharkawy.spring_e_com.dtos.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
        @NotBlank String productId,
        @Positive int quantity
) {
}
