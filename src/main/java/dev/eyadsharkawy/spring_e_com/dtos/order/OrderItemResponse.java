package dev.eyadsharkawy.spring_e_com.dtos.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String productName,
        BigDecimal productPrice,
        int quantity,
        BigDecimal subtotal
) {
}
