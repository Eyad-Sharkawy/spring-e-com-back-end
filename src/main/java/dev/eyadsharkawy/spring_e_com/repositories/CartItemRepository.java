package dev.eyadsharkawy.spring_e_com.repositories;

import dev.eyadsharkawy.spring_e_com.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    @Modifying
    @Transactional
    @Query("delete from CartItem c where c.product.id = ?1")
    void deleteByProductId(String productId);
}