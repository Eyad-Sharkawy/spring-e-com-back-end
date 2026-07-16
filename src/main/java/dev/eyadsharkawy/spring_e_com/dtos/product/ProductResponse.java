package dev.eyadsharkawy.spring_e_com.dtos.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String id,
        String seller,
        String name,
        String description,
        BigDecimal price,
        int stock,
        Instant createdAt,
        Instant updatedAt
) {
}
