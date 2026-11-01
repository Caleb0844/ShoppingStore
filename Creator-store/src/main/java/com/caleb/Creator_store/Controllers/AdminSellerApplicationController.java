package com.caleb.Creator_store.Controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.SellerApplicationService;
import com.caleb.Creator_store.dto.SellerApplicationResponse;
import com.caleb.Creator_store.dto.SellerApplicationReviewRequest;
import com.caleb.Creator_store.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes seller-application review actions only to administrators.
// Keep moderation routes separate from user self-service routes so authorization remains obvious.
@RestController
@RequestMapping("/api/admin/seller-applications")
@RequiredArgsConstructor
public class AdminSellerApplicationController {

    private final SellerApplicationService sellerApplicationService;
    private final CurrentUserService currentUserService;

    // Returns safe seller applications for administrator moderation.
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<SellerApplicationResponse> getApplications() {
        return sellerApplicationService.getAllApplications();
    }

    // Returns one safe seller application for administrator review.
    @GetMapping("/{applicationId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public SellerApplicationResponse getApplication(@PathVariable Long applicationId) {
        return sellerApplicationService.getApplicationById(applicationId);
    }

    // Applies an admin decision to a seller application after method-level role authorization.
    @PutMapping("/{applicationId}/review")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public SellerApplicationResponse reviewApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody SellerApplicationReviewRequest request) {
        return sellerApplicationService.reviewApplication(
                currentUserService.getCurrentUser().userId(), applicationId, request);
    }
}
