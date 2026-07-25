package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.order.OrderResponse;
import dev.eyadsharkawy.spring_e_com.entities.cart.Cart;
import dev.eyadsharkawy.spring_e_com.entities.cart.CartItem;
import dev.eyadsharkawy.spring_e_com.entities.order.Order;
import dev.eyadsharkawy.spring_e_com.entities.order.OrderItem;
import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.EmptyCartException;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.CartRepository;
import dev.eyadsharkawy.spring_e_com.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public OrderResponse checkout(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot checkout and Empty cart");
        }

        Order order = new Order();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            productService.reduceStock(product.getId(), cartItem.getQuantity());

            OrderItem orderItem = OrderItem.createFrom(product, cartItem.getQuantity());

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cart.clearCart();

        return OrderResponse.from(savedOrder);
    }
}
