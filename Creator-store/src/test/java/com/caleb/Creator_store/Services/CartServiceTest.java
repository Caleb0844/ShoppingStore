package com.caleb.Creator_store.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.caleb.Creator_store.Entities.Cart;
import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Repositories.CartRepo;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.CartItemRequest;
import com.caleb.Creator_store.dto.CartResponse;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

// Verifies customer cart ownership and active-product rules without a database or HTTP server.
// Reuse this pattern for later checkout validation around cart contents.
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CartService cartService;

    // Confirms adding an active product creates a customer-owned cart line and calculates totals.
    @Test
    void addItemCreatesCartLineAndCalculatesTotal() {
        Product product = product(10L);
        User customer = User.builder().id(1L).build();
        Cart cart = Cart.builder().id(5L).customer(customer).items(new ArrayList<>()).build();
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepo.save(cart)).thenReturn(cart);

        CartResponse response = cartService.addItem(1L, request(10L, 2));

        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("50.00"), response.getTotal());
        assertEquals(2, response.getItems().get(0).getQuantity());
        verify(cartRepo).save(cart);
    }

    // Confirms archived products cannot be added to a cart.
    @Test
    void addItemRejectsArchivedProduct() {
        Cart cart = Cart.builder().id(5L).items(new ArrayList<>()).build();
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(1L, request(10L, 1)));

        verify(cartRepo, never()).save(cart);
    }

    // Confirms a customer can update only a line in their own cart.
    @Test
    void updateItemReplacesQuantity() {
        Product product = product(10L);
        Cart cart = cartWithProduct(product, 1);
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepo.save(cart)).thenReturn(cart);

        CartResponse response = cartService.updateItem(1L, 10L, request(10L, 4));

        assertEquals(4, response.getItems().get(0).getQuantity());
    }

    // Confirms a missing cart line cannot be manipulated by product ID.
    @Test
    void removeItemRejectsMissingLine() {
        Cart cart = Cart.builder().id(5L).items(new ArrayList<>()).build();
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, 10L));
    }

    private CartItemRequest request(Long productId, int quantity) {
        return CartItemRequest.builder().productId(productId).quantity(quantity).build();
    }

    private Product product(Long id) {
        return Product.builder()
                .id(id)
                .name("Handmade Bag")
                .price(new BigDecimal("25.00"))
                .category("Accessories")
                .stockQuantity(5)
                .active(true)
                .sellerProfile(SellerProfile.builder()
                        .id(30L)
                        .status(SellerApplicationStatus.APPROVED)
                        .build())
                .build();
    }

    private Cart cartWithProduct(Product product, int quantity) {
        Cart cart = Cart.builder().id(5L).items(new ArrayList<>()).build();
        cart.getItems().add(com.caleb.Creator_store.Entities.CartItem.builder()
                .id(7L)
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build());
        return cart;
    }
}
