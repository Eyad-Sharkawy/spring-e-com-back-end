package dev.eyadsharkawy.spring_e_com.repositories;

import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
