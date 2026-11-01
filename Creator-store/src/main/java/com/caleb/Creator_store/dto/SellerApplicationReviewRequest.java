package com.caleb.Creator_store.dto;

import com.caleb.Creator_store.Enums.SellerApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the admin-controlled decision submitted when reviewing a seller application.
// Keep role assignment and transition validation inside the service, not in this DTO.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationReviewRequest {

    @NotNull(message = "Review status is required")
    private SellerApplicationStatus status;

    private String reviewNotes;
}
