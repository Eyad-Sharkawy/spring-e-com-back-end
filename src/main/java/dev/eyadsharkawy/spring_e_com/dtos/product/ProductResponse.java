package dev.eyadsharkawy.spring_e_com.dtos.product;

import dev.eyadsharkawy.spring_e_com.entities.product.Product;

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
        Instant updatedAt,
        String imageUrl,
        String slug
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSeller(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getImageUrl(),
                product.getSlug()
        );
    }
}
