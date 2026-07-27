package dev.eyadsharkawy.spring_e_com.repositories;

import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT p FROM Product p ORDER BY CASE WHEN p.stock > 0 THEN 0 ELSE 1 END ASC")
    Page<Product> findAll(Pageable pageable);
}
