package com.caleb.Creator_store.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.CartService;
import com.caleb.Creator_store.dto.CartItemRequest;
import com.caleb.Creator_store.dto.CartResponse;
import com.caleb.Creator_store.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes customer-owned cart operations using the authenticated JWT identity.
// No route accepts a customer ID, preventing cart access through identifier substitution.
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    // Returns or initializes the authenticated customer's cart.
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public CartResponse getCart() {
        return cartService.getCart(currentUserService.getCurrentUser().userId());
    }

    // Adds a product or increases its existing cart quantity.
    @PostMapping("/items")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(
                currentUserService.getCurrentUser().userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Replaces a product's quantity in the authenticated customer's cart.
    @PutMapping("/items/{productId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public CartResponse updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody CartItemRequest request) {
        return cartService.updateItem(
                currentUserService.getCurrentUser().userId(), productId, request);
    }

    // Removes a product line from the authenticated customer's cart.
    @DeleteMapping("/items/{productId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public CartResponse removeItem(@PathVariable Long productId) {
        return cartService.removeItem(
                currentUserService.getCurrentUser().userId(), productId);
    }
}
