package dev.eyadsharkawy.spring_e_com.controllers;

import dev.eyadsharkawy.spring_e_com.dtos.order.OrderResponse;
import dev.eyadsharkawy.spring_e_com.services.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService service;

    @PostMapping("/{cartId}")
    public ResponseEntity<OrderResponse> checkout(@PathVariable String cartId) {
        OrderResponse orderResponse = service.checkout(cartId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
}
