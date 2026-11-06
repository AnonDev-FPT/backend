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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Wallet, Subscriptions, Pricing Plans & Quota Management (UC-10, BR-22, BR-23)")
public class BillingController {
    private final BillingService billingService;

    @GetMapping("/plans")
    @Operation(summary = "Get list of available subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.ok(billingService.getAllPlans()));
    }

    @PostMapping("/mock-topup")
    @Operation(summary = "Simulated credit wallet top-up")
    public ResponseEntity<ApiResponse<BigDecimal>> mockTopup(@Valid @RequestBody MockTopupRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Top-up successful", request.getAmount()));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to Basic, Pro, or Ultra plan")
    public ResponseEntity<ApiResponse<CompanySubscription>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Subscription activated", null));
    }
}
