package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.cart.CartDto;
import dev.eyadsharkawy.spring_e_com.dtos.cart.CartItemResponse;
import dev.eyadsharkawy.spring_e_com.entities.cart.Cart;
import dev.eyadsharkawy.spring_e_com.entities.product.Product;
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
    private static final Map<String, Comparator<CartItemResponse>> SORT_COMPARATORS = Map.of(
            "productName", Comparator.comparing(CartItemResponse::productName, String.CASE_INSENSITIVE_ORDER),
            "productPrice", Comparator.comparing(CartItemResponse::productPrice),
            "quantity", Comparator.comparingInt(CartItemResponse::quantity),
            "subTotal", Comparator.comparing(CartItemResponse::subTotal),
            "createdAt", Comparator.comparing(CartItemResponse::createdAt)
    );

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private Comparator<CartItemResponse> buildComparator(String sortBy, String direction) {
        Comparator<CartItemResponse> comparator = SORT_COMPARATORS.getOrDefault(
                sortBy, SORT_COMPARATORS.get("createdAt"));

        return "desc".equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
    }

    @Transactional
    public CartDto addProduct(String cartId, String productId, int quantityToAdd) {
        Cart cart = findCartOrThrow(cartId);
        Product product = findProductOrThrow(productId);

        cart.addOrUpdateProduct(product, quantityToAdd);

        return CartDto.from(cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeProductFromCart(String cartId, String productId) {
        Cart cart = findCartOrThrow(cartId);

        cart.removeProductById(productId);

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
        Cart cart = findCartOrThrow(cartId);

        cart.updateItemQuantity(productId, newQuantity);

        return CartDto.from(cartRepository.save(cart));
    }
}
