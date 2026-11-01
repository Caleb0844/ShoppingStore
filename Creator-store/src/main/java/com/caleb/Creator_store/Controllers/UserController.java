package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.UserService;
import com.caleb.Creator_store.dto.UserResponse;

import lombok.RequiredArgsConstructor;

// Provides administrator-only user inspection after public registration moved to AuthController.
// Keep account creation in RegistrationService so password hashing and role defaults cannot be bypassed.
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // Lists users for restricted administrative workflows.
    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    // Retrieves one user for restricted administrative workflows.
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id, null);
    }
}
