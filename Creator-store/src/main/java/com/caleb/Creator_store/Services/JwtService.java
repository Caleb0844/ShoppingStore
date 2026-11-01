package com.caleb.Creator_store.Services;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.UserRole;
import com.caleb.Creator_store.exceptions.InvalidTokenException;
import com.caleb.Creator_store.security.JwtTokenInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import javax.crypto.SecretKey;

// Creates signed JWT access tokens containing only the identity and roles needed by the API.
// Reuse this service from login; keep the signing secret in environment configuration, never source code.
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    // Builds the signing key from a Base64 secret and validates the configured token lifetime.
    // Changing the secret invalidates existing tokens, while changing expiration affects session duration.
    public JwtService(
            @Value("${modestoe.jwt.secret}") String encodedSecret,
            @Value("${modestoe.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero");
        }
        this.expirationMs = expirationMs;
    }

    // Generates a signed token for the authenticated user and embeds roles for later authorization checks.
    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);
        List<String> roles = user.getRoles().stream()
                .map(UserRole::name)
                .toList();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // Verifies a signed token and extracts its typed identity and roles for future request authentication.
    // The parser rejects altered and expired tokens; callers must not bypass this method.
    public JwtTokenInfo parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            if (email == null || email.isBlank()) {
                throw new InvalidTokenException();
            }
            Set<UserRole> roles = extractRoles(claims);
            return new JwtTokenInfo(userId, email, roles);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException(exception);
        }
    }

    // Converts verified role claims into the domain enum set used by authorization checks.
    // Rejecting malformed role claims prevents a token with unexpected data from gaining permissions.
    private Set<UserRole> extractRoles(Claims claims) {
        Object rawRoles = claims.get("roles");
        if (!(rawRoles instanceof List<?> roleValues)) {
            throw new InvalidTokenException();
        }

        Set<UserRole> roles = new HashSet<>();
        for (Object roleValue : roleValues) {
            if (!(roleValue instanceof String roleName)) {
                throw new InvalidTokenException();
            }
            try {
                roles.add(UserRole.valueOf(roleName));
            } catch (IllegalArgumentException exception) {
                throw new InvalidTokenException(exception);
            }
        }
        return roles;
    }
}
