package com.anondev.platform.modules.company.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.company.dto.KybVerificationRequest;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.service.CompanyService;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get Company Profile")
    public ResponseEntity<ApiResponse<Company>> getMyCompany(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyService.getCompanyByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(company));
    }

    @PutMapping("/me/kyb")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Submit KYB Verification")
    public ResponseEntity<ApiResponse<Company>> updateKyb(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody KybVerificationRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company updated = companyService.updateKyb(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("KYB data submitted for verification", updated));
    }

    @PutMapping("/{companyId}/verify-tax")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify Company Tax ID")
    public ResponseEntity<ApiResponse<Company>> verifyTaxId(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "true") boolean isVerified
    ) {
        Company company = companyService.verifyCompanyTaxId(companyId, isVerified);
        return ResponseEntity.ok(ApiResponse.ok("Company tax status updated", company));
    }
}
