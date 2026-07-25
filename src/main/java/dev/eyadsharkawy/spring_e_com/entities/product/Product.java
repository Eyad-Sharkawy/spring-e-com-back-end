package dev.eyadsharkawy.spring_e_com.entities.product;

import dev.eyadsharkawy.spring_e_com.exceptions.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String slug;

    private String seller;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;
    private int stock;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_public_id")
    private String imagePublicId;

    public void decreaseStock(int quantityBought) {
        if (stock < quantityBought) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + this.name + ". Only " + this.stock + " left."
            );
        }
        stock -= quantityBought;
    }

    public void updateImage(String newImageUrl, String newImagePublicId) {
        this.imageUrl = newImageUrl;
        this.imagePublicId = newImagePublicId;
    }
}
