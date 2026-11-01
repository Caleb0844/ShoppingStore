package com.caleb.Creator_store.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.caleb.Creator_store.exceptions.InvalidTokenException;

// Resolves the verified JWT principal for services that need the current user's identity.
// Reuse this boundary instead of accepting user IDs from request bodies, paths, or query parameters.
@Service
public class CurrentUserService {

    // Returns the authenticated JWT identity or rejects an unauthenticated/misconfigured request.
    public JwtTokenInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtTokenInfo tokenInfo)) {
            throw new InvalidTokenException();
        }
        return tokenInfo;
    }
}
