package dev.eyadsharkawy.spring_e_com.dtos.order;

import dev.eyadsharkawy.spring_e_com.entities.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getSubTotal()
                ))
                .toList();

        return new OrderResponse(order.getId(), itemResponses, order.getTotalAmount(), order.getCreatedAt());
    }
}
