package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.product.ProductRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductResponse;
import dev.eyadsharkawy.spring_e_com.entities.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.InsufficientStockException;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price", "stock", "createdAt", "updatedAt");

    private final ProductRepository productRepository;

    private final CloudinaryService cloudinaryService;

    private ProductResponse mapToDto(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSeller(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getImageUrl()
        );
    }

    private Product getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public List<ProductResponse> getAllProducts(String sortBy, String direction) {
        String safeSortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "updatedAt";

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(sortDirection, safeSortField);

        return productRepository.findAll(sort)
                .stream()
                .sorted(Comparator.comparing(product -> product.getStock() == 0))
                .map(this::mapToDto)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        Product product = getProductEntityById(id);
        return mapToDto(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        Product product = getProductEntityById(id);

        if (product.getImagePublicId() != null) {
            cloudinaryService.deleteImage(product.getImagePublicId());
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

    @Transactional
    public ProductResponse updateProductImage(String id, MultipartFile file) {
        Product product = getProductEntityById(id);

        if (product.getImagePublicId() != null) {
            cloudinaryService.deleteImage(product.getImagePublicId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file);
        product.setImageUrl(result.url());
        product.setImagePublicId(result.publicId());

        return mapToDto(product);
    }
}
