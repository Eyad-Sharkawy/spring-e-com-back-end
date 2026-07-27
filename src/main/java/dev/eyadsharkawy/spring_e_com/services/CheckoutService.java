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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling order checkouts.
 * Converts shopping carts to persistent order logs, performs stock validation,
 * and clears the cart after a successful checkout transaction.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    /**
     * Executes the checkout process for a given cart.
     * Starts a database transaction to:
     * 1. Retrieve the cart and verify it's not empty.
     * 2. Reduce the stock of each product by the quantity in the cart.
     * 3. Construct an Order with OrderItems copying current product details (name, price, etc.) for history.
     * 4. Save the order and clear the cart.
     *
     * @param cartId The UUID of the cart to checkout.
     * @return OrderResponse containing details of the created order.
     * @throws ResourceNotFoundException  if the cart does not exist.
     * @throws EmptyCartException         if the cart contains no items.
     * @throws InsufficientStockException if stock falls short during reduction.
     */
    @Transactional
    public OrderResponse checkout(String cartId) {
        Cart cart = cartRepository.findByIdWithItemsAndProducts(cartId)
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
