package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.cart.CartDto;
import dev.eyadsharkawy.spring_e_com.dtos.cart.CartItemResponse;
import dev.eyadsharkawy.spring_e_com.entities.cart.Cart;
import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.CartRepository;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;

/**
 * Service class responsible for managing shopping cart operations.
 * Handles adding/removing items, updating item quantities with stock checks,
 * and sorting cart line items.
 */
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

    /**
     * Builds a sorting comparator based on field and direction.
     */
    private Comparator<CartItemResponse> buildComparator(String sortBy, String direction) {
        Comparator<CartItemResponse> comparator = SORT_COMPARATORS.getOrDefault(
                sortBy, SORT_COMPARATORS.get("createdAt"));

        return "desc".equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
    }

    /**
     * Adds a product to the cart or increments its quantity if already present.
     * Performs a stock check to prevent adding more than the available inventory.
     *
     * @param cartId        The UUID of the cart.
     * @param productId     The UUID of the product.
     * @param quantityToAdd The quantity of the product to add.
     * @return Updated CartDto.
     * @throws ResourceNotFoundException  if cart or product is not found.
     * @throws InsufficientStockException if requested stock exceeds available inventory.
     */
    @Transactional
    public CartDto addProduct(String cartId, String productId, int quantityToAdd) {
        Cart cart = findCartOrThrow(cartId);
        Product product = findProductOrThrow(productId);

        cart.addOrUpdateProduct(product, quantityToAdd);

        return CartDto.from(cartRepository.save(cart));
    }

    /**
     * Removes a product completely from the shopping cart.
     *
     * @param cartId    The UUID of the cart.
     * @param productId The UUID of the product to remove.
     * @return Updated CartDto.
     * @throws ResourceNotFoundException if the cart is not found.
     */
    @Transactional
    public CartDto removeProductFromCart(String cartId, String productId) {
        Cart cart = findCartOrThrow(cartId);

        cart.removeProductById(productId);

        return CartDto.from(cartRepository.save(cart));
    }

    /**
     * Retrieves cart details with its items sorted according to the parameters.
     *
     * @param cartId    The UUID of the cart.
     * @param sortBy    The field to sort the items by.
     * @param direction The sorting direction ("asc" or "desc").
     * @return Sorted CartDto representation.
     * @throws ResourceNotFoundException if the cart does not exist.
     */
    @Transactional(readOnly = true)
    public CartDto getCartDisplay(String cartId, String sortBy, String direction) {
        Cart cart = findCartOrThrow(cartId);
        Comparator<CartItemResponse> comparator = buildComparator(sortBy, direction);
        return CartDto.from(cart, comparator);
    }

    /**
     * Helper to fetch a cart by ID or throw.
     */
    private Cart findCartOrThrow(String cartId) {
        return cartRepository.findByIdWithItemsAndProducts(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
    }

    /**
     * Helper to fetch a product by ID or throw.
     */
    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    /**
     * Updates the quantity of a specific item inside the cart with stock verification.
     *
     * @param cartId      The UUID of the cart.
     * @param productId   The UUID of the product to update.
     * @param newQuantity The new absolute quantity for the cart item.
     * @return Updated CartDto.
     * @throws ResourceNotFoundException  if cart or product is not in the cart.
     * @throws InsufficientStockException if the new quantity exceeds available stock.
     */
    @Transactional
    public CartDto updateItemQuantity(String cartId, String productId, int newQuantity) {
        Cart cart = findCartOrThrow(cartId);

        cart.updateItemQuantity(productId, newQuantity);

        return CartDto.from(cartRepository.save(cart));
    }
}
