package dev.eyadsharkawy.spring_e_com.controllers.v2;

import dev.eyadsharkawy.spring_e_com.dtos.cart.CartDto;
import dev.eyadsharkawy.spring_e_com.dtos.cart.CartItemRequest;
import dev.eyadsharkawy.spring_e_com.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
@Tag(name = "Carts V2", description = "Operations related to shopping cart management (V2)")
public class CartControllerV2 {
    private final CartService cartService;

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart details", description = "Retrieves items in the shopping cart sorted by specified parameter and direction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cart details"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<CartDto> getCart(
            @Parameter(description = "UUID of the cart", example = "af01b535-0d4f-4995-a55e-d95a2c5c5c1a")
            @PathVariable String cartId,
            @Parameter(description = "Field to sort the items by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(cartService.getCartDisplay(cartId, sortBy, direction));
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the specified shopping cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request details"),
            @ApiResponse(responseCode = "404", description = "Cart or Product not found")
    })
    public ResponseEntity<CartDto> addItemToCart(
            @Parameter(description = "UUID of the cart", example = "af01b535-0d4f-4995-a55e-d95a2c5c5c1a")
            @PathVariable String cartId,
            @Valid @RequestBody CartItemRequest request) {
        CartDto updatedCart = cartService.addProduct(cartId, request.productId(), request.quantity());
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific product inside the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity specified"),
            @ApiResponse(responseCode = "404", description = "Cart or product not found in cart")
    })
    public ResponseEntity<CartDto> updateItemQuantity(
            @Parameter(description = "UUID of the cart", example = "af01b535-0d4f-4995-a55e-d95a2c5c5c1a")
            @PathVariable String cartId,
            @Parameter(description = "UUID of the product in the cart", example = "456f7890-e89b-12d3-a456-426614174000")
            @PathVariable String productId,
            @Parameter(description = "New quantity for the item", example = "3")
            @RequestParam int quantity) {
        CartDto updatedCart = cartService.updateItemQuantity(cartId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Removes a specific product from the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from cart successfully"),
            @ApiResponse(responseCode = "404", description = "Cart or product not found in cart")
    })
    public ResponseEntity<CartDto> removeItemFromCart(
            @Parameter(description = "UUID of the cart", example = "af01b535-0d4f-4995-a55e-d95a2c5c5c1a")
            @PathVariable String cartId,
            @Parameter(description = "UUID of the product to remove", example = "456f7890-e89b-12d3-a456-426614174000")
            @PathVariable String productId) {
        CartDto updatedCart = cartService.removeProductFromCart(cartId, productId);
        return ResponseEntity.ok(updatedCart);
    }
}
