package com.caleb.Creator_store.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.SellerProfileRepo;
import com.caleb.Creator_store.dto.ProductRequest;
import com.caleb.Creator_store.dto.ProductResponse;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

// Verifies seller product ownership rules without requiring a database or HTTP server.
// Reuse this unit-test pattern for catalog authorization and mapping behavior.
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private SellerProfileRepo sellerProfileRepo;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductService productService;

    // Confirms an approved seller can create a product owned by their seller profile.
    @Test
    void createProductRequiresApprovedSellerAndSetsOwnership() {
        Long userId = 1L;
        SellerProfile sellerProfile = SellerProfile.builder()
                .id(20L)
                .status(SellerApplicationStatus.APPROVED)
                .build();
        ProductRequest request = productRequest();

        when(sellerProfileRepo.findByUserIdAndStatus(
                userId, SellerApplicationStatus.APPROVED))
                .thenReturn(Optional.of(sellerProfile));
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(30L);
            return product;
        });

        ProductResponse response = productService.createProduct(userId, request);

        assertEquals(30L, response.getId());
        assertEquals(20L, response.getSellerProfileId());
        assertEquals("Handmade Bag", response.getName());
        verify(productRepo).save(any(Product.class));
    }

    // Confirms pending or rejected applicants cannot create catalog products.
    @Test
    void createProductRejectsUnapprovedSeller() {
        Long userId = 1L;
        when(sellerProfileRepo.findByUserIdAndStatus(
                userId, SellerApplicationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessRuleException.class,
                () -> productService.createProduct(userId, productRequest()));

        verify(productRepo, never()).save(any(Product.class));
    }

    // Confirms a seller cannot update another seller's product.
    @Test
    void updateProductRejectsProductOwnedByAnotherSeller() {
        Long userId = 1L;
        Long productId = 30L;
        when(productRepo.findByIdAndActiveTrueAndSellerProfileUserId(productId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(userId, productId, productRequest()));

        verify(productRepo, never()).save(any(Product.class));
    }

    // Confirms a seller can update only the fields exposed by the product request DTO.
    @Test
    void updateProductChangesSellerOwnedFields() {
        Long userId = 1L;
        Long productId = 30L;
        SellerProfile sellerProfile = SellerProfile.builder().id(20L).build();
        Product product = Product.builder()
                .id(productId)
                .name("Old Bag")
                .category("Old")
                .price(new BigDecimal("10.00"))
                .stockQuantity(2)
                .sellerProfile(sellerProfile)
                .build();
        ProductRequest request = productRequest();

        when(productRepo.findByIdAndActiveTrueAndSellerProfileUserId(productId, userId))
                .thenReturn(Optional.of(product));
        when(productRepo.save(product)).thenReturn(product);

        ProductResponse response = productService.updateProduct(userId, productId, request);

        assertEquals("Handmade Bag", response.getName());
        assertEquals("Accessories", response.getCategory());
        assertEquals(new BigDecimal("25.00"), response.getPrice());
        assertEquals(8, response.getStockQuantity());
        assertEquals(20L, response.getSellerProfileId());
    }

    // Confirms archiving hides a seller-owned product without deleting its database record.
    @Test
    void archiveProductMarksOwnedProductInactive() {
        Long userId = 1L;
        Long productId = 30L;
        Product product = Product.builder()
                .id(productId)
                .sellerProfile(SellerProfile.builder().id(20L).build())
                .active(true)
                .build();
        when(productRepo.findByIdAndSellerProfileUserId(productId, userId))
                .thenReturn(Optional.of(product));
        when(productRepo.save(product)).thenReturn(product);

        productService.archiveProduct(userId, productId);

        assertEquals(false, product.isActive());
        verify(productRepo).save(product);
        verify(auditLogService).record(1L, AuditAction.PRODUCT_ARCHIVED,
                "PRODUCT", 30L, "Product archived");
    }

    // Confirms a seller cannot archive a product owned by another seller.
    @Test
    void archiveProductRejectsProductOwnedByAnotherSeller() {
        Long userId = 1L;
        Long productId = 30L;
        when(productRepo.findByIdAndSellerProfileUserId(productId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.archiveProduct(userId, productId));

        verify(productRepo, never()).save(any(Product.class));
    }

    // Confirms public product reads return safe DTOs with no persistence relationship payload.
    @Test
    void getProductByIdReturnsSafeResponse() {
        SellerProfile sellerProfile = SellerProfile.builder().id(20L).build();
        Product product = Product.builder()
                .id(30L)
                .name("Handmade Bag")
                .description("A woven bag")
                .category("Accessories")
                .price(new BigDecimal("25.00"))
                .stockQuantity(8)
                .active(true)
                .sellerProfile(sellerProfile)
                .build();
        when(productRepo.findByIdAndActiveTrue(30L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(30L);

        assertEquals(30L, response.getId());
        assertEquals(20L, response.getSellerProfileId());
        assertEquals("A woven bag", response.getDescription());
        assertEquals(true, response.isActive());
    }

    private ProductRequest productRequest() {
        return ProductRequest.builder()
                .name(" Handmade Bag ")
                .description("A woven bag")
                .category(" Accessories ")
                .price(new BigDecimal("25.00"))
                .stockQuantity(8)
                .build();
    }
}
