package dev.eyadsharkawy.spring_e_com.controllers;

import dev.eyadsharkawy.spring_e_com.dtos.cart.CartDto;
import dev.eyadsharkawy.spring_e_com.dtos.cart.CartItemRequest;
import dev.eyadsharkawy.spring_e_com.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCartDisplay(cartId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDto> addItemToCart(
            @PathVariable String cartId,
            @Valid @RequestBody CartItemRequest request) {
        CartDto updatedCart = cartService.addProduct(cartId, request.productId(), request.quantity());
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @PathVariable String cartId,
            @PathVariable String productId,
            @RequestParam int quantity) {
        CartDto updatedCart = cartService.updateItemQuantity(cartId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            @PathVariable String cartId,
            @PathVariable String productId) {
        CartDto updatedCart = cartService.removeProductFromCart(cartId, productId);
        return ResponseEntity.ok(updatedCart);
    }
}
