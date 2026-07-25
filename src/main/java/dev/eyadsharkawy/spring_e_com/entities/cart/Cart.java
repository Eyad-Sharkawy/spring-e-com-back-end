package dev.eyadsharkawy.spring_e_com.entities.cart;

import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.InsufficientStockException;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public void removeProductById(String productId) {
        CartItem itemToRemove = this.items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (itemToRemove != null) {
            this.removeItem(itemToRemove);
        }
    }

    public void addOrUpdateProduct(Product product, int quantityToAdd) {
        CartItem existingItem = this.items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int totalRequested = (existingItem != null ? existingItem.getQuantity() : 0) + quantityToAdd;

        if (totalRequested > product.getStock()) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName() + ". Only " + product.getStock() + " left."
            );
        }

        if (existingItem != null) {
            existingItem.setQuantity(totalRequested);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantityToAdd);
            this.addItem(newItem);
        }
    }

    public void updateItemQuantity(String productId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CartItem item = this.items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not in cart"));

        if (newQuantity > item.getProduct().getStock()) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + item.getProduct().getName() + ". Only " + item.getProduct().getStock() + " left."
            );
        }
        item.setQuantity(newQuantity);
    }

    public void clearCart() {
        new ArrayList<>(this.items).forEach(this::removeItem);
    }
}
