package com.anondev.platform.modules.matching.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.matching.dto.*;
import com.anondev.platform.modules.matching.service.MatchingUnlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
@Tag(name = "Matching")
public class MatchingController {

    private final MatchingUnlockService matchingUnlockService;

    @GetMapping("/jobs/{jobId}/qualified-candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get Qualified Candidates")
    public ResponseEntity<ApiResponse<List<BlindCandidateCardDto>>> getQualifiedCandidates(
            @PathVariable("jobId") UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<BlindCandidateCardDto> candidates = matchingUnlockService.getQualifiedCandidatesForJob(userDetails.getUsername(), jobId);
        return ResponseEntity.ok(ApiResponse.ok(candidates));
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Send Interview Invitation")
    public ResponseEntity<ApiResponse<InterviewInvitationResponse>> sendInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendInvitationRequest request
    ) {
        InterviewInvitationResponse invitation = matchingUnlockService.sendInvitationByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Invitation sent with hold placed", invitation));
    }

    @GetMapping("/candidates/me/invitations")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get Candidate Invitations")
    public ResponseEntity<ApiResponse<List<CandidateInvitationItemDto>>> getCandidateInvitations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<CandidateInvitationItemDto> invitations = matchingUnlockService.getCandidateInvitationsByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(invitations));
    }

    @PostMapping("/invitations/{id}/respond")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Respond to Invitation")
    public ResponseEntity<ApiResponse<InterviewInvitationResponse>> respondInvitation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RespondInvitationRequest request
    ) {
        InterviewInvitationResponse invitation = matchingUnlockService.respondInvitationByUserEmail(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Invitation response recorded", invitation));
    }

    @GetMapping("/invitations/{id}/unlocked-profile")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get Unlocked Profile")
    public ResponseEntity<ApiResponse<UnlockedCandidateFullDto>> getUnlockedProfile(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UnlockedCandidateFullDto profile = matchingUnlockService.getUnlockedProfileByUserEmail(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/invitations/{id}/hiring-status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update Hiring Status")
    public ResponseEntity<ApiResponse<Void>> updateHiringStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        matchingUnlockService.updateHiringStatusByUserEmail(userDetails.getUsername(), id, status);
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", null));
    }
}
