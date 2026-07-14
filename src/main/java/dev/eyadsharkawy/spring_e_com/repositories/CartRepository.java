package dev.eyadsharkawy.spring_e_com.repositories;

import dev.eyadsharkawy.spring_e_com.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
}
