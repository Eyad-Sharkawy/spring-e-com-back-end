package dev.eyadsharkawy.spring_e_com.entities.order;

import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Setter
@Getter
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String productId;
    private String productName;
    private BigDecimal productPrice;
    private int quantity;
    private BigDecimal subTotal;

    public static OrderItem createFrom(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setSubTotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}