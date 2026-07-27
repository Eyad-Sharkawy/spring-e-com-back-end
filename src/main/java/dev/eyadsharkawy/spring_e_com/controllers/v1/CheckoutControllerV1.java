package dev.eyadsharkawy.spring_e_com.controllers.v1;

import dev.eyadsharkawy.spring_e_com.dtos.order.OrderResponse;
import dev.eyadsharkawy.spring_e_com.services.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout V1", description = "Operations related to order checkout processing (V1 - Deprecated)")
@Deprecated
public class CheckoutControllerV1 {
    private final CheckoutService service;

    @PostMapping("/{cartId}")
    @Operation(summary = "Checkout cart", description = "Processes checkout for the items in the shopping cart and creates a new order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created from cart"),
            @ApiResponse(responseCode = "400", description = "Invalid request details / checkout failed"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<OrderResponse> checkout(
            @Parameter(description = "UUID of the cart to checkout", example = "af01b535-0d4f-4995-a55e-d95a2c5c5c1a")
            @PathVariable String cartId) {
        OrderResponse orderResponse = service.checkout(cartId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
}
