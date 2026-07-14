package dev.eyadsharkawy.spring_e_com.repositories;

import dev.eyadsharkawy.spring_e_com.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
}