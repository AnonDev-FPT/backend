package com.anondev.platform.modules.billing.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.billing.dto.MockTopupRequest;
import com.anondev.platform.modules.billing.dto.SubscribeRequest;
import com.anondev.platform.modules.billing.entity.CompanySubscription;
import com.anondev.platform.modules.billing.entity.SubscriptionPlan;
import com.anondev.platform.modules.billing.service.BillingService;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
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
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

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
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        BigDecimal newBalance = billingService.mockTopup(company.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Top-up successful", newBalance));
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Subscribe Plan")
    public ResponseEntity<ApiResponse<CompanySubscription>> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubscribeRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        CompanySubscription sub = billingService.subscribe(company.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Subscription activated successfully", sub));
    }
}
