package com.caleb.Creator_store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the credentials accepted by the future login endpoint.
// Reuse this DTO for login only; never accept a User entity for credential input.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password can't be empty")
    private String password;
}
