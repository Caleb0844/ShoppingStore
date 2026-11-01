package com.caleb.Creator_store.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.Cart;

// Provides customer-scoped cart persistence.
// The unique customer relationship guarantees one active cart per customer.
public interface CartRepo extends JpaRepository<Cart, Long> {

    // Loads only the cart owned by the authenticated customer.
    Optional<Cart> findByCustomerId(Long customerId);
}
