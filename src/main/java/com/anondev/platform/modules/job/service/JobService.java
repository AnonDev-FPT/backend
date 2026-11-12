package com.anondev.platform.modules.job.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import com.anondev.platform.modules.assessment.repository.TechnicalTestRepository;
import com.anondev.platform.modules.billing.entity.CompanySubscription;
import com.anondev.platform.modules.billing.entity.SubscriptionPlan;
import com.anondev.platform.modules.billing.entity.WalletTransaction;
import com.anondev.platform.modules.billing.repository.CompanySubscriptionRepository;
import com.anondev.platform.modules.billing.repository.SubscriptionPlanRepository;
import com.anondev.platform.modules.billing.repository.WalletTransactionRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.job.dto.CreateJobRequest;
import com.anondev.platform.modules.job.entity.JobPosting;
import com.anondev.platform.modules.job.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final TechnicalTestRepository technicalTestRepository;
    private final AiSanitizerService aiSanitizerService;
    private final CompanySubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public JobPosting createJob(UUID userId, CreateJobRequest request) {
        // Validate salary range consistency
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
            throw new BusinessException("Minimum salary cannot exceed maximum salary.");
        }

        // Lock company record for transaction integrity (BR-24, BR-25)
        Company company = companyRepository.findByOwnerUserIdForUpdate(userId)
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
        String funding = request.getFundingSource() != null ? request.getFundingSource().toUpperCase().trim() : "";
        if ("FREE_TRIAL".equals(funding)) {
            if (company.getHasUsedFreeJob() != null && company.getHasUsedFreeJob()) {
                throw new BusinessException("Free trial job posting already used for this Tax ID (BR-24).");
            }
            company.setHasUsedFreeJob(true);
            companyRepository.save(company);
        } else if ("SINGLE_PURCHASE".equals(funding)) {
            BigDecimal jobFee = BigDecimal.valueOf(100000);
            BigDecimal currentCredit = company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO;
            BigDecimal currentHeld = company.getHeldCreditBalance() != null ? company.getHeldCreditBalance() : BigDecimal.ZERO;
            BigDecimal availableCredit = currentCredit.subtract(currentHeld);

            if (availableCredit.compareTo(jobFee) < 0) {
                throw new BusinessException("Insufficient available wallet balance for single job purchase (100.000 VND required - BR-25). Available: " + availableCredit + " VND");
            }
            company.setCreditBalance(currentCredit.subtract(jobFee));
            companyRepository.save(company);

            walletTransactionRepository.save(WalletTransaction.builder()
                    .companyId(company.getId())
                    .transactionType("SINGLE_JOB_PURCHASE")
                    .amount(jobFee)
                    .paymentGateway("INTERNAL_WALLET")
                    .description("Single job purchase fee for posting: " + request.getTitle())
                    .build());
        } else if ("SUBSCRIPTION_SLOT".equals(funding)) {
            CompanySubscription subscription = subscriptionRepository.findByCompanyIdAndStatus(company.getId(), "ACTIVE")
                    .orElseThrow(() -> new BusinessException("No active subscription plan found to post jobs with SUBSCRIPTION_SLOT (BR-26)."));

            if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(Instant.now())) {
                subscription.setStatus("EXPIRED");
                subscriptionRepository.save(subscription);
                throw new BusinessException("Subscription plan has expired (BR-26). Please renew your subscription to post jobs.");
            }

            SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId())
                    .orElseThrow(() -> new BusinessException("Subscription plan details not found."));

            long currentActiveJobs = jobPostingRepository.countByCompanyIdAndStatus(company.getId(), "ACTIVE");
            if (currentActiveJobs >= plan.getMaxConcurrentJobs()) {
                throw new BusinessException("Maximum concurrent active jobs limit (" + plan.getMaxConcurrentJobs() + ") reached for plan '" + plan.getPlanName() + "' (BR-26). Please upgrade plan or close existing jobs.");
            }
        } else {
            throw new BusinessException("Invalid funding source: " + request.getFundingSource() + ". Allowed: FREE_TRIAL, SINGLE_PURCHASE, SUBSCRIPTION_SLOT");
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
