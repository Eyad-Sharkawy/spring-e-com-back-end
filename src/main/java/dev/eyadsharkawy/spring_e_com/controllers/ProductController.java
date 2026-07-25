package dev.eyadsharkawy.spring_e_com.controllers;

import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinarySignatureResponse;
import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinaryUploadConfirmRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductRequest;
import dev.eyadsharkawy.spring_e_com.dtos.product.ProductResponse;
import dev.eyadsharkawy.spring_e_com.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(productService.getAllProducts(sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        ProductResponse updated = productService.updateProductImage(id, file);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/image/signature")
    public ResponseEntity<CloudinarySignatureResponse> getUploadSignature(@PathVariable String id) {
        return ResponseEntity.ok(productService.getUploadSignature(id));
    }

    @PostMapping("/{id}/image/confirm")
    public ResponseEntity<ProductResponse> confirmUpload(
            @PathVariable String id,
            @Valid @RequestBody CloudinaryUploadConfirmRequest request) {
        return ResponseEntity.ok(productService.confirmProductImage(id, request));
    }
}
