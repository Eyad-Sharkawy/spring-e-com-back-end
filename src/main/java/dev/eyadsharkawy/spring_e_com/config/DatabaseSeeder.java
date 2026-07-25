package dev.eyadsharkawy.spring_e_com.config;

import dev.eyadsharkawy.spring_e_com.entities.Product;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import dev.eyadsharkawy.spring_e_com.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    public void run(String... args) {
        List<Product> products = productRepository.findAll();
        boolean updated = false;
        for (Product product : products) {
            if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
                String slug = productService.generateUniqueSlug(product.getName());
                product.setSlug(slug);
                productRepository.save(product);
                log.info("Generated slug '{}' for product ID {}", slug, product.getId());
                updated = true;
            }
        }
        if (updated) {
            log.info("Finished populating missing slugs for existing products.");
        }
    }
}
