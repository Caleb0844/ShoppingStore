package com.caleb.Creator_store.Services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.UserRole;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.UserRegistrationRequest;
import com.caleb.Creator_store.dto.UserResponse;
import com.caleb.Creator_store.exceptions.BusinessRuleException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Creates customer accounts while enforcing safe credential and role defaults.
// Reuse this service from the future authentication controller; keep hashing out of controllers.
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    // Validates uniqueness, hashes the password, assigns CUSTOMER, and returns a safe account view.
    // This transaction keeps account data and its default role persistence in one operation.
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessRuleException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Set.of(UserRole.CUSTOMER)))
                .build();

        return toResponse(userRepo.save(user));
    }

    // Maps User to the public response contract without exposing the password hash or relationships.
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .build();
    }
}
