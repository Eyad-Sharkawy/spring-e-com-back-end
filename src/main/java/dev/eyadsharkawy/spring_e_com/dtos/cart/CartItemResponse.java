package dev.eyadsharkawy.spring_e_com.dtos.cart;

import dev.eyadsharkawy.spring_e_com.entities.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        String productId,
        String productName,
        String productSeller,
        BigDecimal productPrice,
        int quantity,
        int availableStock,
        BigDecimal subTotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        int quantity = item.getQuantity();
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(quantity));

        return new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSeller(),
                price,
                quantity,
                item.getProduct().getStock(),
                subTotal
        );
    }
}
