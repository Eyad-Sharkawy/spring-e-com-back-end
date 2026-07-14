package dev.eyadsharkawy.spring_e_com.dtos.cart;

import dev.eyadsharkawy.spring_e_com.entities.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        String id,
        List<CartItemResponse> items,
        BigDecimal sumTotal
) {
    public static CartDto from(Cart cart) {
        List<CartItemResponse> itemDtos = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();

        BigDecimal total = itemDtos.stream()
                .map(CartItemResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getId(), itemDtos, total);
    }
}
