package com.anondev.platform.modules.assessment.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.assessment.dto.CreateTestRequest;
import com.anondev.platform.modules.assessment.dto.SubmitTestRequest;
import com.anondev.platform.modules.assessment.dto.TestAttemptSummaryDto;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import com.anondev.platform.modules.assessment.service.AssessmentService;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
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
@Tag(name = "Assessment", description = "Technical Test, Proctoring & Decree 13 Consent APIs (UC-05, UC-06)")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;

    @GetMapping("/{jobId}/test")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get technical test for candidate (Checks BR-15 attempt limits)")
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
    @Operation(summary = "Submit technical test attempt with Decree 13 consent and proctoring telemetry")
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
    @Operation(summary = "Create technical test linked to a job posting")
    public ResponseEntity<ApiResponse<TechnicalTest>> createTest(@Valid @RequestBody CreateTestRequest request) {
        TechnicalTest test = assessmentService.createTest(request);
        return ResponseEntity.ok(ApiResponse.ok("Technical test created successfully", test));
    }
}
