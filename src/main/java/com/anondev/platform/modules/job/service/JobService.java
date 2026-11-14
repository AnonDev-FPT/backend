package com.anondev.platform.modules.job.service;

import com.anondev.platform.modules.job.dto.CreateJobRequest;
import com.anondev.platform.modules.job.dto.JobPostingResponse;

import java.util.List;
import java.util.UUID;

public interface JobService {
    JobPostingResponse createJob(String userEmail, CreateJobRequest request);
    JobPostingResponse createJob(UUID userId, CreateJobRequest request);
    List<JobPostingResponse> getJobsByCompany(String userEmail);
    List<JobPostingResponse> getJobsByCompany(UUID userId);
    List<JobPostingResponse> getActiveJobs();
    JobPostingResponse getJobById(UUID jobId);
}
