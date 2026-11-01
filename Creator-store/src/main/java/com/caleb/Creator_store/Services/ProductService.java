package com.caleb.Creator_store.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.SellerProfileRepo;
import com.caleb.Creator_store.dto.ProductRequest;
import com.caleb.Creator_store.dto.ProductResponse;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Coordinates seller-owned product creation, updates, archiving, and public catalog reads.
// Ownership is checked in this service so it remains enforced even if another controller is added later.
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;
    private final SellerProfileRepo sellerProfileRepo;
    private final AuditLogService auditLogService;

    // Creates a product only for an approved seller profile.
    @Transactional
    public ProductResponse createProduct(Long userId, ProductRequest request) {
        SellerProfile sellerProfile = getApprovedSellerProfile(userId);
        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory().trim())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sellerProfile(sellerProfile)
                .build();

        return toResponse(productRepo.save(product));
    }

    // Updates only a product owned by the authenticated seller.
    @Transactional
    public ProductResponse updateProduct(Long userId, Long productId, ProductRequest request) {
        Product product = productRepo.findByIdAndActiveTrueAndSellerProfileUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found for the authenticated seller"));

        applyRequest(product, request);
        return toResponse(productRepo.save(product));
    }

    // Returns safe public catalog responses without exposing persistence relationships.
    public List<ProductResponse> getProducts() {
        return productRepo.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    // Returns one safe public product response.
    public ProductResponse getProductById(Long id) {
        return productRepo.findByIdAndActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product with id " + id + " not found"));
    }

    // Archives only a product owned by the authenticated seller.
    // The record remains available for future order history and audit requirements.
    @Transactional
    public void archiveProduct(Long userId, Long productId) {
        Product product = productRepo.findByIdAndSellerProfileUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found for the authenticated seller"));
        product.setActive(false);
        productRepo.save(product);
        auditLogService.record(
                userId,
                AuditAction.PRODUCT_ARCHIVED,
                "PRODUCT",
                productId,
                "Product archived");
    }

    private SellerProfile getApprovedSellerProfile(Long userId) {
        return sellerProfileRepo.findByUserIdAndStatus(userId, SellerApplicationStatus.APPROVED)
                .orElseThrow(() -> new BusinessRuleException(
                        "Only approved sellers can manage products"));
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.isActive())
                .sellerProfileId(product.getSellerProfile().getId())
                .build();
    }
}
