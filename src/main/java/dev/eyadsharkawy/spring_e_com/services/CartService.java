package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.cart.CartDto;
import dev.eyadsharkawy.spring_e_com.dtos.cart.CartItemResponse;
import dev.eyadsharkawy.spring_e_com.entities.Cart;
import dev.eyadsharkawy.spring_e_com.entities.CartItem;
import dev.eyadsharkawy.spring_e_com.entities.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.InsufficientStockException;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.CartRepository;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private static final Map<String, Comparator<CartItemResponse>> SORT_COMPARATORS = Map.of(
            "productName", Comparator.comparing(CartItemResponse::productName, String.CASE_INSENSITIVE_ORDER),
            "productPrice", Comparator.comparing(CartItemResponse::productPrice),
            "quantity", Comparator.comparingInt(CartItemResponse::quantity),
            "subTotal", Comparator.comparing(CartItemResponse::subTotal),
            "createdAt", Comparator.comparing(CartItemResponse::createdAt)
    );

    private Comparator<CartItemResponse> buildComparator(String sortBy, String direction) {
        Comparator<CartItemResponse> comparator = SORT_COMPARATORS.getOrDefault(
                sortBy, SORT_COMPARATORS.get("createdAt"));

        return "desc".equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
    }

    @Transactional
    public CartDto addProduct(String cartId, String productId, int quantityToAdd) {
        Cart cart = findCartOrThrow(cartId);
        Product product = findProductOrThrow(productId);

        int currentQuantity = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .map(CartItem::getQuantity)
                .orElse(0);

        int totalRequested = currentQuantity + quantityToAdd;

        if (totalRequested > product.getStock()) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName() + ". Only " + product.getStock() + " left."
            );
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(totalRequested),
                        () -> {
                            CartItem newItem = new CartItem();

                            newItem.setProduct(product);
                            newItem.setQuantity(totalRequested);
                            cart.addItem(newItem);
                        }
                );

        return CartDto.from(cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeProductFromCart(String cartId, String productId) {
        Cart cart = findCartOrThrow(cartId);

        cart.getItems().removeIf(item -> {
            boolean matches = item.getProduct().getId().equals(productId);
            if (matches) {
                item.setCart(null);
            }
            return matches;
        });

        return CartDto.from(cartRepository.save(cart));
    }

    public CartDto getCartDisplay(String cartId, String sortBy, String direction) {
        Cart cart = findCartOrThrow(cartId);
        Comparator<CartItemResponse> comparator = buildComparator(sortBy, direction);
        return CartDto.from(cart, comparator);
    }

    private Cart findCartOrThrow(String cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    @Transactional
    public CartDto updateItemQuantity(String cartId, String productId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = findCartOrThrow(cartId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product " + productId + " not in cart " + cartId));

        int currentStock = item.getProduct().getStock();

        if (newQuantity > currentStock) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + item.getProduct().getName()
                            + ". Only " + item.getProduct().getStock() + " left."
            );
        }

        item.setQuantity(newQuantity);

        return CartDto.from(cartRepository.save(cart));
    }
}
