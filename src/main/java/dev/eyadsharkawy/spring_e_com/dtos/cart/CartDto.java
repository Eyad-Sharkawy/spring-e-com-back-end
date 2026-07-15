package dev.eyadsharkawy.spring_e_com.dtos.cart;

import dev.eyadsharkawy.spring_e_com.entities.Cart;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record CartDto(
        String id,
        List<CartItemResponse> items,
        BigDecimal sumTotal
) {
    public static CartDto from(Cart cart) {
        return from(cart, null);
    }

    public static CartDto from(Cart cart, Comparator<CartItemResponse> comparator) {
        List<CartItemResponse> itemDtos = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();

        if (comparator != null) {
            itemDtos = itemDtos.stream().sorted(comparator).toList();
        }

        BigDecimal total = itemDtos.stream()
                .map(CartItemResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getId(), itemDtos, total);
    }
}
