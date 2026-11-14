package com.anondev.platform.modules.billing.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.billing.dto.RefundDisputeRequest;
import com.anondev.platform.modules.billing.entity.RefundDispute;
import com.anondev.platform.modules.billing.service.DisputeRefundService;
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
@Tag(name = "Dispute")
public class AdminDisputeController {

    private final DisputeRefundService disputeRefundService;

    @PostMapping("/disputes/refund")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Submit Refund Dispute")
    public ResponseEntity<ApiResponse<RefundDispute>> submitDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RefundDisputeRequest request
    ) {
        RefundDispute dispute = disputeRefundService.createDisputeByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Dispute submitted successfully", dispute));
    }

    @PutMapping("/admin/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve Dispute")
    public ResponseEntity<ApiResponse<Void>> resolveDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @RequestParam("approve") boolean approve
    ) {
        disputeRefundService.resolveDisputeByUserEmail(userDetails != null ? userDetails.getUsername() : null, id, approve);
        return ResponseEntity.ok(ApiResponse.ok("Dispute resolution processed: approved=" + approve, null));
    }
}
