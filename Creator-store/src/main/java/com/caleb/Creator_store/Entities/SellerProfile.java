package com.caleb.Creator_store.Entities;

import java.time.LocalDateTime;
import java.util.List;

import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Stores seller-specific public profile data and private application-review state separately from User.
// Reuse this entity for seller onboarding; protect KYC and review fields when changing its API mappings.
@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Display name is required")
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column
    private String bio;

    @NotBlank(message = "Business name is required")
    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @JsonIgnore
    @NotBlank(message = "National ID number is required")
    @Column(name = "national_id_number", nullable = false)
    private String nationalIdNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SellerApplicationStatus status = SellerApplicationStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @JsonIgnore
    @Column(name = "review_notes")
    private String reviewNotes;

    // Provides the seller-owned catalog boundary for ownership checks and future seller dashboards.
    @JsonIgnore
    @OneToMany(mappedBy = "sellerProfile")
    private List<Product> products;
}
