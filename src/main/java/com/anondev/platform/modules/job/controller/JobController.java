package com.anondev.platform.modules.job.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.job.dto.CreateJobRequest;
import com.anondev.platform.modules.job.entity.JobPosting;
import com.anondev.platform.modules.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "Job Posting and Slot Management APIs (UC-04, BR-17, BR-24, BR-26)")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Post a new Job with accompanying Technical Test (checks KYB & slot limit)")
    public ResponseEntity<ApiResponse<JobPosting>> createJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateJobRequest request
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        JobPosting job = jobService.createJob(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Job posting created successfully", job));
    }

    @GetMapping
    @Operation(summary = "List all active job postings")
    public ResponseEntity<ApiResponse<List<JobPosting>>> getActiveJobs() {
        List<JobPosting> jobs = jobService.getActiveJobs();
        return ResponseEntity.ok(ApiResponse.ok(jobs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details by ID")
    public ResponseEntity<ApiResponse<JobPosting>> getJobById(@PathVariable UUID id) {
        JobPosting job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }
}
