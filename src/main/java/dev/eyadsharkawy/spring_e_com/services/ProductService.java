package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.product.ProductRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductResponse;
import dev.eyadsharkawy.spring_e_com.entities.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.InsufficientStockException;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private ProductResponse mapToDto(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSeller(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }

    private Product getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        Product product = getProductEntityById(id);
        return mapToDto(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void reduceStock(String id, int quantityBought) {
        Product product = getProductEntityById(id);
        if (product.getStock() < quantityBought) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName() + ". Only " + product.getStock() + " left."
            );
        }
        product.setStock(product.getStock() - quantityBought);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product newProduct = new Product();
        newProduct.setSeller(request.seller());
        newProduct.setName(request.name());
        newProduct.setDescription(request.description());
        newProduct.setPrice(request.price());
        newProduct.setStock(request.stock());

        Product savedProduct = productRepository.save(newProduct);
        return mapToDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product existingProduct = getProductEntityById(id);

        existingProduct.setSeller(request.seller());
        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setPrice(request.price());
        existingProduct.setStock(request.stock());

        return mapToDto(existingProduct);
    }
}
