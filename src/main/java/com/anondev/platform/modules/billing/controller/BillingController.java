package com.anondev.platform.modules.billing.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.billing.dto.MockTopupRequest;
import com.anondev.platform.modules.billing.dto.SubscribeRequest;
import com.anondev.platform.modules.billing.entity.CompanySubscription;
import com.anondev.platform.modules.billing.entity.SubscriptionPlan;
import com.anondev.platform.modules.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing")
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/plans")
    @Operation(summary = "List Subscription Plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.ok(billingService.getAllPlans()));
    }

    @PostMapping("/mock-topup")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Mock Top-up Wallet")
    public ResponseEntity<ApiResponse<BigDecimal>> mockTopup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MockTopupRequest request
    ) {
        BigDecimal newBalance = billingService.mockTopupByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Top-up successful", newBalance));
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Subscribe Plan")
    public ResponseEntity<ApiResponse<CompanySubscription>> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubscribeRequest request
    ) {
        CompanySubscription subscription = billingService.subscribeByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Subscription updated successfully", subscription));
    }
}
