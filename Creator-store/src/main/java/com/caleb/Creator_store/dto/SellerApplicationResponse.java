package com.caleb.Creator_store.dto;

import java.time.LocalDateTime;

import com.caleb.Creator_store.Enums.SellerApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the safe seller-application data returned to clients.
// Do not add national ID, user links, or internal review notes without an explicit privacy decision.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationResponse {

    private Long id;
    private String displayName;
    private String bio;
    private String businessName;
    private String profileImageUrl;
    private SellerApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
