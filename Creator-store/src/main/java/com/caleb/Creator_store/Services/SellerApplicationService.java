package com.caleb.Creator_store.Services;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Enums.UserRole;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.Repositories.SellerProfileRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.SellerApplicationRequest;
import com.caleb.Creator_store.dto.SellerApplicationResponse;
import com.caleb.Creator_store.dto.SellerApplicationReviewRequest;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Coordinates seller application submission, review transitions, role changes, and safe mapping.
// Reuse this service from controllers; authentication and authorization must guard its future admin entry points.
@Service
@RequiredArgsConstructor
public class SellerApplicationService {

    private final SellerProfileRepo sellerProfileRepo;
    private final UserRepo userRepo;
    private final AuditLogService auditLogService;

    // Creates a single pending application for an existing user.
    // The service owns protected fields so clients cannot approve themselves or change review metadata.
    @Transactional
    public SellerApplicationResponse submitApplication(
            Long userId,
            SellerApplicationRequest request) {
        if (sellerProfileRepo.existsByUserId(userId)) {
            throw new BusinessRuleException("This user already has a seller application");
        }

        SellerProfile application = SellerProfile.builder()
                .user(userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId)))
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .businessName(request.getBusinessName())
                .profileImageUrl(request.getProfileImageUrl())
                .nationalIdNumber(request.getNationalIdNumber())
                .status(SellerApplicationStatus.PENDING)
                .build();

        // Applications always enter review as pending; only the approval workflow may change status.
        return toResponse(sellerProfileRepo.save(application));
    }

    // Returns a safe application view without exposing KYC or internal review data.
    public SellerApplicationResponse getApplicationByUserId(Long userId) {
        SellerProfile application = sellerProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller application not found for user id: " + userId));

        return toResponse(application);
    }

    // Returns safe seller application views for administrator moderation screens.
    public java.util.List<SellerApplicationResponse> getAllApplications() {
        return sellerProfileRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Returns one safe application view for administrator support and moderation.
    public SellerApplicationResponse getApplicationById(Long applicationId) {
        return sellerProfileRepo.findById(applicationId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller application not found with id: " + applicationId));
    }

    // Applies an allowed admin decision and synchronizes the user's seller role.
    // Keep this transactional because profile status and user roles must change together.
    @Transactional
    public SellerApplicationResponse reviewApplication(
            Long applicationId,
            SellerApplicationReviewRequest request) {
        return reviewApplication(null, applicationId, request);
    }

    // Applies an admin decision while recording the authenticated reviewer when available.
    @Transactional
    public SellerApplicationResponse reviewApplication(
            Long reviewerUserId,
            Long applicationId,
            SellerApplicationReviewRequest request) {
        SellerProfile application = sellerProfileRepo.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller application not found with id: " + applicationId));

        SellerApplicationStatus requestedStatus = request.getStatus();
        validateStatusTransition(application.getStatus(), requestedStatus);

        application.setStatus(requestedStatus);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewNotes(request.getReviewNotes());

        User user = application.getUser();
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (requestedStatus == SellerApplicationStatus.APPROVED) {
            user.getRoles().add(UserRole.SELLER);
            auditLogService.record(reviewerUserId, AuditAction.SELLER_APPLICATION_APPROVED,
                    "SELLER_APPLICATION", applicationId, "Application approved");
        } else if (requestedStatus == SellerApplicationStatus.SUSPENDED) {
            user.getRoles().remove(UserRole.SELLER);
            auditLogService.record(reviewerUserId, AuditAction.SELLER_APPLICATION_SUSPENDED,
                    "SELLER_APPLICATION", applicationId, "Application suspended");
        } else if (requestedStatus == SellerApplicationStatus.REJECTED) {
            auditLogService.record(reviewerUserId, AuditAction.SELLER_APPLICATION_REJECTED,
                    "SELLER_APPLICATION", applicationId, "Application rejected");
        }

        return toResponse(sellerProfileRepo.save(application));
    }

    // Enforces the seller-application state machine before any status or role change is persisted.
    private void validateStatusTransition(
            SellerApplicationStatus currentStatus,
            SellerApplicationStatus requestedStatus) {
            if (requestedStatus == null
                    || (requestedStatus != SellerApplicationStatus.APPROVED
                    && requestedStatus != SellerApplicationStatus.REJECTED
                    && requestedStatus != SellerApplicationStatus.SUSPENDED)) {
                throw new BusinessRuleException(
                        "Applications can only be approved, rejected, or suspended");
            }

        boolean reviewingPendingApplication = currentStatus == SellerApplicationStatus.PENDING
                && (requestedStatus == SellerApplicationStatus.APPROVED
                        || requestedStatus == SellerApplicationStatus.REJECTED);
        boolean suspendingApprovedApplication = currentStatus == SellerApplicationStatus.APPROVED
                && requestedStatus == SellerApplicationStatus.SUSPENDED;

        if (!reviewingPendingApplication && !suspendingApprovedApplication) {
            throw new BusinessRuleException(
                    "The application status cannot change from "
                            + currentStatus + " to " + requestedStatus);
        }
    }

    // Maps persistence state to the public response contract.
    // Reuse this mapper for future seller-application endpoints to prevent sensitive-field leaks.
    private SellerApplicationResponse toResponse(SellerProfile application) {
        return SellerApplicationResponse.builder()
                .id(application.getId())
                .displayName(application.getDisplayName())
                .bio(application.getBio())
                .businessName(application.getBusinessName())
                .profileImageUrl(application.getProfileImageUrl())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .reviewedAt(application.getReviewedAt())
                .build();
    }
}
