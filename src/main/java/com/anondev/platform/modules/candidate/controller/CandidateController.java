package com.anondev.platform.modules.candidate.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.candidate.dto.UpdateProfileRequest;
import com.anondev.platform.modules.candidate.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate", description = "Candidate Identity Vault and Blind Profile Management (UC-03)")
public class CandidateController {

    private final CandidateProfileService candidateProfileService;
    private final UserRepository userRepository;

    @GetMapping("/me/vault")
    @Operation(summary = "Get current candidate 2-column vault (PII isolated vs Public)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyVault(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Map<String, Object> data = candidateProfileService.getCandidateVaultData(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PutMapping("/me/vault")
    @Operation(summary = "Update vault details and blind profile with auto AI sanitization (BR-03)")
    public ResponseEntity<ApiResponse<Void>> updateMyVault(
            @AuthenticationPrincipal UserDetails userDetails,
            @jakarta.validation.Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        candidateProfileService.updateCandidateVault(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated and sanitized successfully", null));
    }
}
