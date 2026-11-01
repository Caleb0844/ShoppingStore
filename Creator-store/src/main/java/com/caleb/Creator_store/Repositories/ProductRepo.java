package com.caleb.Creator_store.Repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.Product;


public interface ProductRepo extends JpaRepository<Product, Long>{

    // Returns only products currently visible in the public catalog.
    List<Product> findAllByActiveTrue();

    // Finds a product only when it is active and belongs to the authenticated seller.
    Optional<Product> findByIdAndActiveTrueAndSellerProfileUserId(Long id, Long userId);

    // Returns one active product for public product details.
    Optional<Product> findByIdAndActiveTrue(Long id);

    // Finds a product only when it belongs to the authenticated seller.
    Optional<Product> findByIdAndSellerProfileUserId(Long id, Long userId);

}
