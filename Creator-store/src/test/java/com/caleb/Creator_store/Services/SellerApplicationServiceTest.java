package com.caleb.Creator_store.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Enums.UserRole;
import com.caleb.Creator_store.Repositories.SellerProfileRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.Services.AuditLogService;
import com.caleb.Creator_store.dto.SellerApplicationRequest;
import com.caleb.Creator_store.dto.SellerApplicationResponse;
import com.caleb.Creator_store.dto.SellerApplicationReviewRequest;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

// Verifies seller-application rules without requiring a database or HTTP server.
// Reuse this unit-test pattern for isolated service rules; keep integration tests for persistence behavior.
@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceTest {

    @Mock
    private SellerProfileRepo sellerProfileRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SellerApplicationService sellerApplicationService;

    // Confirms a valid application is created as pending and returned through the safe DTO.
    @Test
    void submitApplicationCreatesPendingApplicationAndHidesSensitiveData() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        SellerApplicationRequest request = SellerApplicationRequest.builder()
                .displayName("Caleb Designs")
                .bio("Handmade clothing")
                .businessName("Caleb Designs Ltd")
                .profileImageUrl("https://example.com/profile.jpg")
                .nationalIdNumber("12345678")
                .build();

        when(sellerProfileRepo.existsByUserId(userId)).thenReturn(false);
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(sellerProfileRepo.save(any(SellerProfile.class)))
                .thenAnswer(invocation -> {
                    SellerProfile saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        SellerApplicationResponse response =
                sellerApplicationService.submitApplication(userId, request);

        assertEquals(10L, response.getId());
        assertEquals("Caleb Designs", response.getDisplayName());
        assertEquals(SellerApplicationStatus.PENDING, response.getStatus());
        verify(sellerProfileRepo).save(any(SellerProfile.class));
    }

    // Confirms one user cannot submit multiple seller applications.
    @Test
    void submitApplicationRejectsDuplicateApplication() {
        Long userId = 1L;
        when(sellerProfileRepo.existsByUserId(userId)).thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> sellerApplicationService.submitApplication(
                        userId,
                        SellerApplicationRequest.builder().build()));

        verify(userRepo, never()).findById(any());
        verify(sellerProfileRepo, never()).save(any());
    }

    // Confirms applications cannot be created for users that do not exist.
    @Test
    void submitApplicationRejectsUnknownUser() {
        Long userId = 99L;
        when(sellerProfileRepo.existsByUserId(userId)).thenReturn(false);
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> sellerApplicationService.submitApplication(
                        userId,
                        SellerApplicationRequest.builder().build()));

        verify(sellerProfileRepo, never()).save(any());
    }

    // Confirms application lookup maps entity state without exposing sensitive fields.
    @Test
    void getApplicationByUserIdReturnsSafeResponse() {
        Long userId = 1L;
        SellerProfile application = SellerProfile.builder()
                .id(10L)
                .user(User.builder().id(userId).build())
                .displayName("Caleb Designs")
                .businessName("Caleb Designs Ltd")
                .nationalIdNumber("12345678")
                .status(SellerApplicationStatus.PENDING)
                .build();
        when(sellerProfileRepo.findByUserId(userId)).thenReturn(Optional.of(application));

        SellerApplicationResponse response =
                sellerApplicationService.getApplicationByUserId(userId);

        assertEquals(10L, response.getId());
        assertEquals(SellerApplicationStatus.PENDING, response.getStatus());
        assertEquals("Caleb Designs", response.getDisplayName());
    }

    // Confirms approval grants the seller capability to the applicant.
    @Test
    void approveApplicationGrantsSellerRole() {
        User user = User.builder()
                .id(1L)
                .build();
        SellerProfile application = SellerProfile.builder()
                .id(10L)
                .user(user)
                .status(SellerApplicationStatus.PENDING)
                .build();
        when(sellerProfileRepo.findById(10L)).thenReturn(Optional.of(application));
        when(sellerProfileRepo.save(application)).thenReturn(application);

        SellerApplicationResponse response = sellerApplicationService.reviewApplication(
                10L,
                SellerApplicationReviewRequest.builder()
                        .status(SellerApplicationStatus.APPROVED)
                        .reviewNotes("Verified")
                        .build());

        assertEquals(SellerApplicationStatus.APPROVED, response.getStatus());
        assertTrue(user.getRoles().contains(UserRole.SELLER));
    }

    // Confirms rejection leaves the applicant without seller capability.
    @Test
    void rejectApplicationDoesNotGrantSellerRole() {
        User user = User.builder()
                .id(1L)
                .build();
        SellerProfile application = SellerProfile.builder()
                .id(10L)
                .user(user)
                .status(SellerApplicationStatus.PENDING)
                .build();
        when(sellerProfileRepo.findById(10L)).thenReturn(Optional.of(application));
        when(sellerProfileRepo.save(application)).thenReturn(application);

        sellerApplicationService.reviewApplication(
                10L,
                SellerApplicationReviewRequest.builder()
                        .status(SellerApplicationStatus.REJECTED)
                        .reviewNotes("More information required")
                        .build());

        assertFalse(user.getRoles().contains(UserRole.SELLER));
    }

    // Confirms suspension removes seller capability from an approved seller.
    @Test
    void suspendApplicationRemovesSellerRole() {
        User user = User.builder()
                .id(1L)
                .roles(new java.util.HashSet<>(
                        java.util.Set.of(UserRole.CUSTOMER, UserRole.SELLER)))
                .build();
        SellerProfile application = SellerProfile.builder()
                .id(10L)
                .user(user)
                .status(SellerApplicationStatus.APPROVED)
                .build();
        when(sellerProfileRepo.findById(10L)).thenReturn(Optional.of(application));
        when(sellerProfileRepo.save(application)).thenReturn(application);

        sellerApplicationService.reviewApplication(
                10L,
                SellerApplicationReviewRequest.builder()
                        .status(SellerApplicationStatus.SUSPENDED)
                        .reviewNotes("Policy violation")
                        .build());

        assertFalse(user.getRoles().contains(UserRole.SELLER));
    }

    // Confirms the service rejects an invalid state transition.
    @Test
    void cannotApproveAlreadyRejectedApplication() {
        SellerProfile application = SellerProfile.builder()
                .id(10L)
                .user(User.builder().id(1L).build())
                .status(SellerApplicationStatus.REJECTED)
                .build();
        when(sellerProfileRepo.findById(10L)).thenReturn(Optional.of(application));

        assertThrows(
                BusinessRuleException.class,
                () -> sellerApplicationService.reviewApplication(
                        10L,
                        SellerApplicationReviewRequest.builder()
                                .status(SellerApplicationStatus.APPROVED)
                                .build()));
    }
}
