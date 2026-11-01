package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.ProductService;
import com.caleb.Creator_store.dto.ProductRequest;
import com.caleb.Creator_store.dto.ProductResponse;
import com.caleb.Creator_store.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes public catalog reads and authenticated seller product management.
// Keep seller identity derived from the JWT instead of accepting it from request data.
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CurrentUserService currentUserService;

    // Creates a product for the authenticated approved seller.
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(
                currentUserService.getCurrentUser().userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Updates a product only when it belongs to the authenticated seller.
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SELLER')")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(
                currentUserService.getCurrentUser().userId(), id, request);
    }

    // Keeps product browsing public for customers and unauthenticated visitors.
    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    // Keeps individual product browsing public.
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // Archives a product only when it belongs to the authenticated seller.
    @PatchMapping("/{id}/archive")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> archiveProduct(@PathVariable Long id) {
        productService.archiveProduct(currentUserService.getCurrentUser().userId(), id);
        return ResponseEntity.noContent().build();
    }
}
