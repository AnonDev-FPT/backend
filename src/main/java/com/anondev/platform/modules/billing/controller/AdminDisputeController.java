package com.anondev.platform.modules.billing.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.billing.dto.RefundDisputeRequest;
import com.anondev.platform.modules.billing.entity.RefundDispute;
import com.anondev.platform.modules.billing.service.DisputeRefundService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Dispute", description = "Refund Dispute & Admin Arbitration (BR-18, BR-19, BR-20)")
public class AdminDisputeController {

    private final DisputeRefundService disputeRefundService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @PostMapping("/disputes/refund")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Submit refund dispute within 7 days of unlock (BR-18, BR-20)")
    public ResponseEntity<ApiResponse<RefundDispute>> submitDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RefundDisputeRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for current user"));

        RefundDispute dispute = disputeRefundService.createDispute(company.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Dispute submitted successfully", dispute));
    }

    @PutMapping("/admin/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin resolves dispute: approve (credit/quota refund) or reject (BR-19)")
    public ResponseEntity<ApiResponse<Void>> resolveDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @RequestParam("approve") boolean approve
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        disputeRefundService.resolveDispute(user.getId(), id, approve);
        return ResponseEntity.ok(ApiResponse.ok("Dispute resolution processed: approved=" + approve, null));
    }
}
