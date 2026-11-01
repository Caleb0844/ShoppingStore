package com.caleb.Creator_store.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.RegistrationService;
import com.caleb.Creator_store.Services.AuthenticationService;
import com.caleb.Creator_store.Services.JwtService;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.dto.LoginRequest;
import com.caleb.Creator_store.dto.LoginResponse;
import com.caleb.Creator_store.dto.UserRegistrationRequest;
import com.caleb.Creator_store.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes public account-registration operations without exposing the User persistence entity.
// Reuse this controller for future authentication endpoints while keeping business rules in services.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    // Validates registration input and returns the newly created customer account.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Verifies credentials, creates a signed JWT, and returns only safe account information.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authenticationService.authenticate(request);
        String accessToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .roles(user.getRoles())
                        .build())
                .build();
    }
}
