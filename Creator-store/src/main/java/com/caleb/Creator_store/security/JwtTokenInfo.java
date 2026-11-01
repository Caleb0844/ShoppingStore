package com.caleb.Creator_store.security;

import java.util.Set;

import com.caleb.Creator_store.Enums.UserRole;

// Carries only the verified identity and authorization data extracted from a JWT.
// Reuse this immutable value when a security filter or service needs the authenticated principal.
public record JwtTokenInfo(
        Long userId,
        String email,
        Set<UserRole> roles) {
}
