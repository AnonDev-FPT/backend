package com.anondev.platform.modules.matching.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.matching.dto.*;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
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
@Tag(name = "Matching", description = "2-Way Mutual Unlock, Invitations & 14-day Tracking (BR-02, BR-06, BR-07, BR-09)")
public class MatchingController {

    private final MatchingUnlockService matchingUnlockService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/jobs/{jobId}/qualified-candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get qualified candidates (BlindCandidateCardDto - BR-01)")
    public ResponseEntity<ApiResponse<List<BlindCandidateCardDto>>> getQualifiedCandidates(
            @PathVariable("jobId") UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        List<BlindCandidateCardDto> candidates = matchingUnlockService.getQualifiedCandidatesForJob(jobId, company.getId());
        return ResponseEntity.ok(ApiResponse.ok(candidates));
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Send interview invitation with hold credit/quota (BR-06)")
    public ResponseEntity<ApiResponse<InterviewInvitation>> sendInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendInvitationRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        InterviewInvitation invitation = matchingUnlockService.sendInvitation(company.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Invitation sent with hold placed", invitation));
    }

    @GetMapping("/candidates/me/invitations")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Candidate retrieves their incoming interview invitations")
    public ResponseEntity<ApiResponse<List<CandidateInvitationItemDto>>> getCandidateInvitations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        List<CandidateInvitationItemDto> invitations = matchingUnlockService.getCandidateInvitations(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(invitations));
    }

    @PostMapping("/invitations/{id}/respond")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Candidate responds ACCEPT (unlocks) or REJECT (refunds hold) (BR-06)")
    public ResponseEntity<ApiResponse<InterviewInvitation>> respondInvitation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RespondInvitationRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        InterviewInvitation invitation = matchingUnlockService.respondInvitation(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Invitation response recorded", invitation));
    }

    @GetMapping("/invitations/{id}/unlocked-profile")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Employer views decrypted profile after ACCEPT_UNLOCKED (BR-02)")
    public ResponseEntity<ApiResponse<UnlockedCandidateFullDto>> getUnlockedProfile(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        UnlockedCandidateFullDto profile = matchingUnlockService.getUnlockedProfile(company.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/invitations/{id}/hiring-status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update 14-day post-unlock status (BR-09)")
    public ResponseEntity<ApiResponse<Void>> updateHiringStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        matchingUnlockService.updateHiringStatus(company.getId(), id, status);
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", null));
    }
}
