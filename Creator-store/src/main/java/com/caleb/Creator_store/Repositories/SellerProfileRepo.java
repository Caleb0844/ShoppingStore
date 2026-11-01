package com.caleb.Creator_store.Repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;

// Provides persistence queries for the one seller application/profile allowed per user.
// Add derived queries here when seller-profile lookups are needed by services.
public interface SellerProfileRepo extends JpaRepository<SellerProfile, Long> {

    // Supports duplicate-application prevention before a new profile is saved.
    boolean existsByUserId(Long userId);

    // Retrieves the application associated with a specific user.
    Optional<SellerProfile> findByUserId(Long userId);

    // Loads only an approved seller profile for product creation.
    Optional<SellerProfile> findByUserIdAndStatus(Long userId, SellerApplicationStatus status);

    // Supports administrator dashboards that filter seller applications by lifecycle state.
    List<SellerProfile> findAllByStatus(SellerApplicationStatus status);
}
