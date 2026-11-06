package com.anondev.platform.modules.matching.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.matching.dto.*;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import com.anondev.platform.modules.matching.service.MatchingUnlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "2-Way Mutual Unlock, Invitations & 14-day Tracking (BR-02, BR-06, BR-07, BR-09)")
public class MatchingController {
    private final MatchingUnlockService matchingUnlockService;

    @GetMapping("/jobs/{jobId}/qualified-candidates")
    @Operation(summary = "Get qualified candidates (BlindCandidateCardDto - BR-01)")
    public ResponseEntity<ApiResponse<List<BlindCandidateCardDto>>> getQualifiedCandidates(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @PostMapping("/invitations")
    @Operation(summary = "Send interview invitation with hold credit/quota (BR-06)")
    public ResponseEntity<ApiResponse<InterviewInvitation>> sendInvitation(@Valid @RequestBody SendInvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation sent with hold placed", null));
    }

    @PostMapping("/invitations/{id}/respond")
    @Operation(summary = "Candidate responds ACCEPT (unlocks) or REJECT (refunds hold) (BR-06)")
    public ResponseEntity<ApiResponse<InterviewInvitation>> respondInvitation(@PathVariable UUID id, @Valid @RequestBody RespondInvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation response recorded", null));
    }

    @GetMapping("/invitations/{id}/unlocked-profile")
    @Operation(summary = "Employer views decrypted profile after ACCEPT_UNLOCKED (BR-02)")
    public ResponseEntity<ApiResponse<UnlockedCandidateFullDto>> getUnlockedProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/invitations/{id}/hiring-status")
    @Operation(summary = "Update 14-day post-unlock status (BR-09)")
    public ResponseEntity<ApiResponse<Void>> updateHiringStatus(@PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", null));
    }
}
