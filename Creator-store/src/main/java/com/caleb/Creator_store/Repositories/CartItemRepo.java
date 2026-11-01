package com.caleb.Creator_store.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.CartItem;

// Supports locating one product line inside a customer's cart.
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    // Prevents duplicate cart lines when a customer adds the same product again.
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
