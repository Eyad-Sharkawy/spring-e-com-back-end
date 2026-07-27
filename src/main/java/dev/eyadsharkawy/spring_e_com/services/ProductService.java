package dev.eyadsharkawy.spring_e_com.services;

import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinarySignatureResponse;
import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinaryUploadConfirmRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductResponse;
import dev.eyadsharkawy.spring_e_com.entities.product.Product;
import dev.eyadsharkawy.spring_e_com.exceptions.ResourceNotFoundException;
import dev.eyadsharkawy.spring_e_com.repositories.CartItemRepository;
import dev.eyadsharkawy.spring_e_com.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service class responsible for managing catalog products.
 * Handles product CRUD operations, SEO URL slug generation,
 * stock reductions, and Cloudinary image upload workflows.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price", "stock", "createdAt", "updatedAt");

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    private final CloudinaryService cloudinaryService;

    /**
     * Helper to fetch a product entity or throw an exception if not found.
     */
    private Product getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    /**
     * Retrieves all products sorted by specified properties.
     * Implements two-level sorting: returning active in-stock products first,
     * followed by out-of-stock products, with each group ordered by the chosen sort parameter.
     *
     * @param sortBy    Field to sort the products by.
     * @param direction Sort order ("asc" or "desc").
     * @return List of sorted ProductResponse objects.
     */
    public List<ProductResponse> getAllProducts(String sortBy, String direction) {
        String safeSortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "updatedAt";

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(sortDirection, safeSortField);

        return productRepository.findAll(sort)
                .stream()
                .sorted(Comparator.comparing(product -> product.getStock() == 0))
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Finds a single product by either its unique SEO URL slug or its UUID ID.
     *
     * @param identifier Slug or ID of the product.
     * @return ProductResponse matching the identifier.
     * @throws ResourceNotFoundException if no matching product is found.
     */
    public ProductResponse getProductBySlugOrId(String identifier) {
        Product product = productRepository.findBySlug(identifier)
                .orElseGet(() -> productRepository.findById(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with identifier: " + identifier)));
        return ProductResponse.from(product);
    }

    /**
     * Deletes a product from the database.
     * Safely deletes the image from Cloudinary (if present) and cascades
     * to remove references from active shopping carts before deleting the database record.
     *
     * @param id The UUID of the product to delete.
     * @throws ResourceNotFoundException if the product does not exist.
     */
    public void deleteProduct(String id) {
        Product product = getProductEntityById(id);
        String imagePublicId = product.getImagePublicId();

        deleteCloudinaryImageSafely(imagePublicId);

        cartItemRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    /**
     * Decreases the inventory stock of a product by the specified quantity.
     *
     * @param id             The UUID of the product.
     * @param quantityBought The quantity purchased.
     * @throws InsufficientStockException if the purchase quantity exceeds available stock.
     */
    @Transactional
    public void reduceStock(String id, int quantityBought) {
        Product product = getProductEntityById(id);

        product.decreaseStock(quantityBought);

        productRepository.save(product);
    }

    /**
     * Creates a new product and generates a unique URL slug based on its name.
     *
     * @param request DTO containing product creation parameters.
     * @return ProductResponse for the newly created product.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product newProduct = new Product();
        newProduct.setSeller(request.seller());
        newProduct.setName(request.name());
        newProduct.setDescription(request.description());
        newProduct.setPrice(request.price());
        newProduct.setStock(request.stock());
        newProduct.setSlug(generateUniqueSlug(request.name()));

        Product savedProduct = productRepository.save(newProduct);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Updates an existing product's attributes. Re-generates the URL slug
     * if the product name has changed to keep URL references aligned.
     *
     * @param id      The UUID of the product to update.
     * @param request DTO containing new product properties.
     * @return ProductResponse of the updated product.
     */
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product existingProduct = getProductEntityById(id);

        if (existingProduct.getSlug() == null || !existingProduct.getName().equalsIgnoreCase(request.name())) {
            existingProduct.setSlug(generateUniqueSlug(request.name()));
        }

        existingProduct.setSeller(request.seller());
        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setPrice(request.price());
        existingProduct.setStock(request.stock());

        return ProductResponse.from(existingProduct);
    }

    /**
     * Uploads a new product image directly from the server to Cloudinary.
     * Automatically handles clean-up of the old image asset on Cloudinary.
     *
     * @param id   The UUID of the product.
     * @param file The multipart image file.
     * @return ProductResponse containing the updated image URL.
     */
    public ProductResponse updateProductImage(String id, MultipartFile file) {
        Product product = getProductEntityById(id);
        String oldImagePublicId = product.getImagePublicId();

        deleteCloudinaryImageSafely(oldImagePublicId);

        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file);

        product.updateImage(result.url(), result.publicId());

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Generates a secure, cryptographically signed upload signature for Cloudinary.
     * This signature is returned to the frontend to allow uploading images directly
     * from the browser to Cloudinary without routing the file through the backend server.
     *
     * @param productId The UUID of the product for which the signature is generated.
     * @return CloudinarySignatureResponse containing authorization parameters and signature.
     */
    public CloudinarySignatureResponse getUploadSignature(String productId) {
        getProductEntityById(productId);
        return cloudinaryService.generateSignature(productId);
    }

    /**
     * Confirms a frontend image upload.
     * Updates the product record with the uploaded image URL and public ID returned by Cloudinary,
     * while safely deleting any previous image asset on Cloudinary.
     *
     * @param id      The UUID of the product.
     * @param request DTO containing the confirmed image URL and public ID.
     * @return ProductResponse containing updated product details.
     */
    public ProductResponse confirmProductImage(String id, CloudinaryUploadConfirmRequest request) {
        Product product = getProductEntityById(id);
        String oldImagePublicId = product.getImagePublicId();

        deleteCloudinaryImageSafely(oldImagePublicId);

        product.updateImage(request.url(), request.publicId());

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Converts a raw string (usually a product name) into a URL-friendly SEO slug.
     * Removes special characters, replaces whitespaces with hyphens, and formats to lowercase.
     */
    public String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Generates a unique SEO URL slug by checking database conflicts.
     * Appends a random 4-character suffix in case of collision.
     */
    public String generateUniqueSlug(String name) {
        String baseSlug = generateSlug(name);
        if (baseSlug.isEmpty()) {
            baseSlug = "product";
        }
        String candidateSlug = baseSlug;
        while (productRepository.existsBySlug(candidateSlug)) {
            String randomSuffix = generateRandomString();
            candidateSlug = baseSlug + "-" + randomSuffix;
        }
        return candidateSlug;
    }

    /**
     * Helper to generate a random 4-character alphanumeric string.
     */
    private String generateRandomString() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Safely deletes an image asset from Cloudinary without failing the database transaction.
     */
    private void deleteCloudinaryImageSafely(String imagePublicId) {
        if (imagePublicId != null) {
            try {
                cloudinaryService.deleteImage(imagePublicId);
            } catch (Exception e) {
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }
    }
}
