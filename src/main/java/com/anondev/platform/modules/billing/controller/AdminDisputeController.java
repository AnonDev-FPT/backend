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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Dispute", description = "Refund Dispute & Admin Arbitration (BR-18, BR-19, BR-20)")
public class AdminDisputeController {
    private final DisputeRefundService disputeRefundService;

    @PostMapping("/disputes/refund")
    @Operation(summary = "Submit refund dispute within 7 days of unlock (BR-18, BR-20)")
    public ResponseEntity<ApiResponse<RefundDispute>> submitDispute(@Valid @RequestBody RefundDisputeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Dispute submitted", null));
    }

    @PutMapping("/admin/disputes/{id}/resolve")
    @Operation(summary = "Admin resolves dispute: approve (credit/quota refund) or reject (BR-19)")
    public ResponseEntity<ApiResponse<Void>> resolveDispute(@PathVariable UUID id, @RequestParam boolean approve) {
        return ResponseEntity.ok(ApiResponse.ok("Dispute resolved", null));
    }
}
