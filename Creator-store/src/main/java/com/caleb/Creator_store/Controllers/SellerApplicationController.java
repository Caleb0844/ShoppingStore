package com.caleb.Creator_store.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.SellerApplicationService;
import com.caleb.Creator_store.dto.SellerApplicationRequest;
import com.caleb.Creator_store.dto.SellerApplicationResponse;
import com.caleb.Creator_store.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes seller-application HTTP operations using the authenticated user identity.
// Reuse this pattern for personal resources so clients cannot impersonate another user by changing an ID.
@RestController
@RequestMapping("/api/seller-applications")
@RequiredArgsConstructor
public class SellerApplicationController {

    private final SellerApplicationService sellerApplicationService;
    private final CurrentUserService currentUserService;

    // Accepts a validated application and returns its initial pending state.
    @PostMapping("/me")
    public ResponseEntity<SellerApplicationResponse> submitApplication(
            @Valid @RequestBody SellerApplicationRequest request) {
        Long userId = currentUserService.getCurrentUser().userId();
        SellerApplicationResponse response = sellerApplicationService.submitApplication(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Returns the safe application view for the authenticated user.
    @GetMapping("/me")
    public SellerApplicationResponse getApplication() {
        Long userId = currentUserService.getCurrentUser().userId();
        return sellerApplicationService.getApplicationByUserId(userId);
    }
}
