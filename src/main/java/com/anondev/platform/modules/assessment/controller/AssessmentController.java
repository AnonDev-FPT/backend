package com.anondev.platform.modules.assessment.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.assessment.dto.CreateTestRequest;
import com.anondev.platform.modules.assessment.dto.SubmitTestRequest;
import com.anondev.platform.modules.assessment.dto.TestAttemptSummaryDto;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import com.anondev.platform.modules.assessment.service.AssessmentService;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/{jobId}/test")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get Job Test")
    public ResponseEntity<ApiResponse<TechnicalTest>> getJobTest(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required to take technical test");
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        TechnicalTest test = assessmentService.getJobTest(jobId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(test));
    }

    @PostMapping("/{jobId}/test/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit Test Attempt")
    public ResponseEntity<ApiResponse<TestAttemptSummaryDto>> submitJobTest(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubmitTestRequest request
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required to submit technical test");
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        TestAttemptSummaryDto summary = assessmentService.submitTest(jobId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Test attempt evaluated successfully", summary));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create Technical Test")
    public ResponseEntity<ApiResponse<TechnicalTest>> createTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTestRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        TechnicalTest test = assessmentService.createTest(company.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Technical test created successfully", test));
    }
}
