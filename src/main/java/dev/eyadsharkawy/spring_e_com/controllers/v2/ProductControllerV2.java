package dev.eyadsharkawy.spring_e_com.controllers.v2;

import dev.eyadsharkawy.spring_e_com.dtos.common.PageResponse;
import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinarySignatureResponse;
import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinaryUploadConfirmRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductResponse;
import dev.eyadsharkawy.spring_e_com.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@Tag(name = "Products V2", description = "Operations related to product catalog management with pagination")
public class ProductControllerV2 {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products paginated", description = "Retrieves a paginated list of products sorted by the specified parameter and direction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products list")
    })
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Field to sort the products by", example = "updatedAt")
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(page, size, sortBy, direction));
    }

    @GetMapping("/{identifier}")
    @Operation(summary = "Get product by ID or Slug", description = "Retrieves details of a product using either its slug or database UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found and returned"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "The product UUID or unique slug", example = "wireless-headphones")
            @PathVariable String identifier) {
        return ResponseEntity.ok(productService.getProductBySlugOrId(identifier));
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Adds a new product to the catalog.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input details provided")
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product", description = "Updates details of a product specified by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input details provided"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "UUID of the product to update", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Removes a product from the catalog by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "UUID of the product to delete", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Upload product image", description = "Uploads a product image directly.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> uploadProductImage(
            @Parameter(description = "UUID of the product", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        ProductResponse updated = productService.updateProductImage(id, file);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/image/signature")
    @Operation(summary = "Get Cloudinary upload signature", description = "Generates a signed signature for secure front-end direct uploading to Cloudinary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature generated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<CloudinarySignatureResponse> getUploadSignature(
            @Parameter(description = "UUID of the product", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id) {
        return ResponseEntity.ok(productService.getUploadSignature(id));
    }

    @PostMapping("/{id}/image/confirm")
    @Operation(summary = "Confirm product image upload", description = "Confirms that the image has been successfully uploaded to Cloudinary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image confirmation processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request details"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> confirmUpload(
            @Parameter(description = "UUID of the product", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @Valid @RequestBody CloudinaryUploadConfirmRequest request) {
        return ResponseEntity.ok(productService.confirmProductImage(id, request));
    }
}
