package com.anondev.platform.modules.company.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.company.dto.CompanyProfileResponse;
import com.anondev.platform.modules.company.dto.KybVerificationRequest;
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

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get Company Profile")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> getMyCompany(@AuthenticationPrincipal UserDetails userDetails) {
        CompanyProfileResponse company = companyService.getCompanyProfileByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(company));
    }

    @PutMapping("/me/kyb")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Submit KYB Verification")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> updateKyb(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody KybVerificationRequest request
    ) {
        CompanyProfileResponse updated = companyService.updateKybByUserEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("KYB data submitted for verification", updated));
    }

    @PutMapping("/{companyId}/verify-tax")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify Company Tax ID")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> verifyTaxId(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "true") boolean isVerified
    ) {
        CompanyProfileResponse company = companyService.verifyCompanyTaxId(companyId, isVerified);
        return ResponseEntity.ok(ApiResponse.ok("Company tax status updated", company));
    }
}
