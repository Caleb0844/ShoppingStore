package com.caleb.Creator_store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the client-controlled fields accepted when a user applies to become a seller.
// Keep protected identity, status, timestamps, and review fields out of this request DTO.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationRequest {

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String bio;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String profileImageUrl;

    @NotBlank(message = "National ID number is required")
    private String nationalIdNumber;
}
