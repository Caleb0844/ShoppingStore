package com.caleb.Creator_store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the public account-registration input without exposing the User persistence entity.
// Reuse this contract for registration; keep password handling in the authentication service.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {

    @NotBlank(message = "First name can't be empty")
    private String firstName;

    @NotBlank(message = "Last name can't be empty")
    private String lastName;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Phone number can't be empty")
    @Pattern(
            regexp = "0[17]\\d{8}",
            message = "Phone number must be 10 digits and start with 01 or 07")
    private String phoneNumber;

    @NotBlank(message = "Password can't be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
