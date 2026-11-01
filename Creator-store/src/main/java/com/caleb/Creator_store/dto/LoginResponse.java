package com.caleb.Creator_store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the authenticated account information that can accompany a future JWT response.
// Keep tokens and account data separate so token claims can evolve without exposing persistence fields.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private UserResponse user;
}
