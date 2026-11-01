package com.caleb.Creator_store.Services;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.Cart;
import com.caleb.Creator_store.Entities.CartItem;
import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Repositories.CartRepo;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.CartItemRequest;
import com.caleb.Creator_store.dto.CartItemResponse;
import com.caleb.Creator_store.dto.CartResponse;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Coordinates customer-owned cart creation, updates, and removal of cart lines.
// This service keeps product availability and ownership checks out of controllers.
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepo cartRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;

    // Adds an active product to the customer's cart or increases its existing quantity.
    @Transactional
    public CartResponse addItem(Long customerId, CartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);
        Product product = getActiveProduct(request.getProductId());
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });
        item.setQuantity(item.getQuantity() + request.getQuantity());
        return toResponse(cartRepo.save(cart));
    }

    // Returns the cart owned by the authenticated customer.
    @Transactional
    public CartResponse getCart(Long customerId) {
        return toResponse(getOrCreateCart(customerId));
    }

    // Replaces one cart-line quantity after confirming the product is still active.
    @Transactional
    public CartResponse updateItem(Long customerId, Long productId, CartItemRequest request) {
        Cart cart = getExistingCart(customerId);
        getActiveProduct(productId);
        CartItem item = findItem(cart, productId);
        item.setQuantity(request.getQuantity());
        return toResponse(cartRepo.save(cart));
    }

    // Removes one product line from the authenticated customer's cart.
    @Transactional
    public CartResponse removeItem(Long customerId, Long productId) {
        Cart cart = getExistingCart(customerId);
        CartItem item = findItem(cart, productId);
        cart.getItems().remove(item);
        return toResponse(cartRepo.save(cart));
    }

    private Cart getOrCreateCart(Long customerId) {
        return cartRepo.findByCustomerId(customerId).orElseGet(() -> {
            User customer = userRepo.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with id: " + customerId));
            return cartRepo.save(Cart.builder()
                    .customer(customer)
                    .items(new ArrayList<>())
                    .build());
        });
    }

    private Cart getExistingCart(Long customerId) {
        return cartRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Product getActiveProduct(Long productId) {
        return productRepo.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active product not found with id: " + productId));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product is not in the customer's cart"));
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .sellerProfileId(item.getProduct().getSellerProfile().getId())
                            .quantity(item.getQuantity())
                            .currentPrice(item.getProduct().getPrice())
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .total(total)
                .build();
    }
}
