package com.anondev.platform.modules.job.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import com.anondev.platform.modules.assessment.repository.TechnicalTestRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.job.dto.CreateJobRequest;
import com.anondev.platform.modules.job.entity.JobPosting;
import com.anondev.platform.modules.job.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final TechnicalTestRepository technicalTestRepository;
    private final AiSanitizerService aiSanitizerService;

    @Transactional
    public JobPosting createJob(UUID userId, CreateJobRequest request) {
        Company company = companyRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));

        // BR-17: Verified Tax ID required
        if (company.getIsTaxVerified() == null || !company.getIsTaxVerified()) {
            throw new BusinessException("Company Tax ID (KYB) must be verified before posting jobs (BR-17).");
        }

        // BR-09: Check blocked status due to overdue 14-day update
        if (company.getIsBlockedByOverdueStatus() != null && company.getIsBlockedByOverdueStatus()) {
            throw new BusinessException("Job posting blocked: Company has overdue candidate status updates (BR-09).");
        }

        // Validate funding source & slots (BR-24, BR-25, BR-26)
        if ("FREE_TRIAL".equalsIgnoreCase(request.getFundingSource())) {
            if (company.getHasUsedFreeJob() != null && company.getHasUsedFreeJob()) {
                throw new BusinessException("Free trial job posting already used for this Tax ID (BR-24).");
            }
            company.setHasUsedFreeJob(true);
            companyRepository.save(company);
        } else if ("SINGLE_PURCHASE".equalsIgnoreCase(request.getFundingSource())) {
            BigDecimal jobFee = BigDecimal.valueOf(100000);
            if (company.getCreditBalance().compareTo(jobFee) < 0) {
                throw new BusinessException("Insufficient wallet balance for single job purchase (100.000 VND required - BR-25).");
            }
            company.setCreditBalance(company.getCreditBalance().subtract(jobFee));
            companyRepository.save(company);
        }

        // BR-03: Sanitize job description
        String sanitizedDesc = aiSanitizerService.sanitizeText(request.getDescription());

        JobPosting job = JobPosting.builder()
                .companyId(company.getId())
                .title(request.getTitle())
                .descriptionSanitized(sanitizedDesc)
                .provinceCity(request.getProvinceCity())
                .workMode(request.getWorkMode())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .minPassScore(request.getMinPassScore() != null ? request.getMinPassScore() : BigDecimal.valueOf(50.00))
                .fundingSource(request.getFundingSource().toUpperCase())
                .status("ACTIVE")
                .build();
        job = jobPostingRepository.save(job);

        // BR-11: Create accompanying Technical Test
        TechnicalTest test = TechnicalTest.builder()
                .jobPostingId(job.getId())
                .title(request.getTestTitle() != null ? request.getTestTitle() : "Technical Test: " + job.getTitle())
                .creationMethod("MANUAL")
                .problemStatement(request.getProblemStatement() != null ? request.getProblemStatement() : "Complete the algorithmic assignment.")
                .timeLimitMinutes(request.getTimeLimitMinutes() != null ? request.getTimeLimitMinutes() : 45)
                .starterCodeJson(request.getStarterCodeJson() != null ? request.getStarterCodeJson() : "{}")
                .testCasesJson(request.getTestCasesJson() != null ? request.getTestCasesJson() : "[]")
                .build();
        technicalTestRepository.save(test);

        return job;
    }

    @Transactional(readOnly = true)
    public List<JobPosting> getActiveJobs() {
        return jobPostingRepository.findByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public JobPosting getJobById(UUID jobId) {
        return jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job posting not found"));
    }
}
