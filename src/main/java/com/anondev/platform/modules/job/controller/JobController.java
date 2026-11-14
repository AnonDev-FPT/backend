package com.anondev.platform.modules.job.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.job.dto.CreateJobRequest;
import com.anondev.platform.modules.job.dto.JobPostingResponse;
import com.anondev.platform.modules.job.service.JobService;
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
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create Job")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobPostingResponse job = jobService.createJob(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Job posting created successfully", job));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get list of jobs posted by current employer")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<JobPostingResponse> jobs = jobService.getJobsByCompany(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(jobs));
    }

    @GetMapping
    @Operation(summary = "List Active Jobs")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getActiveJobs() {
        List<JobPostingResponse> jobs = jobService.getActiveJobs();
        return ResponseEntity.ok(ApiResponse.ok(jobs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Job Details")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getJobById(@PathVariable UUID id) {
        JobPostingResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }
}
