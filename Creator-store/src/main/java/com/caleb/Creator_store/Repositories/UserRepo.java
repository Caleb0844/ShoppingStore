package com.caleb.Creator_store.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.User;


public interface UserRepo extends  JpaRepository<User, Long>{

    // Supports fast duplicate-account checks before registration and reinforces the database unique constraint.
    boolean existsByEmailIgnoreCase(String email);

    // Retrieves the account needed to verify credentials during login.
    Optional<User> findByEmailIgnoreCase(String email);

}
