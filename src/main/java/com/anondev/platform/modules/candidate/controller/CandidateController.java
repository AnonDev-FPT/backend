package com.anondev.platform.modules.candidate.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.candidate.dto.UpdateProfileRequest;
import com.anondev.platform.modules.candidate.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate")
public class CandidateController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping("/me/vault")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get Candidate Vault")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyVault(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> data = candidateProfileService.getCandidateVaultDataByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PutMapping("/me/vault")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update Candidate Vault")
    public ResponseEntity<ApiResponse<Void>> updateMyVault(
            @AuthenticationPrincipal UserDetails userDetails,
            @jakarta.validation.Valid @RequestBody UpdateProfileRequest request
    ) {
        candidateProfileService.updateCandidateVaultByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated and sanitized successfully", null));
    }
}
