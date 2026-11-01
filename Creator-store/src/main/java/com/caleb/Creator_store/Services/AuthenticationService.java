package com.caleb.Creator_store.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.LoginRequest;
import com.caleb.Creator_store.exceptions.InvalidCredentialsException;

import lombok.RequiredArgsConstructor;

// Verifies login credentials and returns the authenticated User for JWT creation.
// Reuse this credential check from authentication flows; keep password comparison in one service.
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    // Normalizes email, loads the account, and compares the raw password with the stored BCrypt hash.
    // Always use one generic failure so callers cannot discover whether an email exists.
    public User authenticate(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
