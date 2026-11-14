package com.anondev.platform.modules.assessment.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.assessment.dto.CreateTestRequest;
import com.anondev.platform.modules.assessment.dto.SubmitTestRequest;
import com.anondev.platform.modules.assessment.dto.TestAttemptSummaryDto;
import com.anondev.platform.modules.assessment.entity.ProctoringLog;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import com.anondev.platform.modules.assessment.entity.TestAttempt;
import com.anondev.platform.modules.assessment.repository.ProctoringLogRepository;
import com.anondev.platform.modules.assessment.repository.TechnicalTestRepository;
import com.anondev.platform.modules.assessment.repository.TestAttemptRepository;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.job.entity.JobPosting;
import com.anondev.platform.modules.job.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final TechnicalTestRepository technicalTestRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final ProctoringLogRepository proctoringLogRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AiSanitizerService aiSanitizerService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public TechnicalTest getJobTestByUserEmail(UUID jobId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        return getJobTest(jobId, user.getId());
    }

    @Transactional
    public TestAttemptSummaryDto submitTestByUserEmail(UUID jobId, String userEmail, SubmitTestRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        return submitTest(jobId, user.getId(), request);
    }

    @Transactional
    public TechnicalTest createTestByUserEmail(String userEmail, CreateTestRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        return createTest(company.getId(), request);
    }

    @Transactional(readOnly = true)
    public TechnicalTest getJobTest(UUID jobId, UUID candidateId) {
        // BR-15 Attempt Limit Rule
        if (testAttemptRepository.existsByJobPostingIdAndCandidateId(jobId, candidateId)) {
            throw new BusinessException("You have already completed or started the technical test for this job (BR-15).");
        }

        return technicalTestRepository.findByJobPostingId(jobId)
                .orElseThrow(() -> new BusinessException("No technical test found for this job posting."));
    }

    @Transactional
    public TechnicalTest createTest(UUID companyId, CreateTestRequest request) {
        JobPosting job = jobPostingRepository.findById(request.getJobPostingId())
                .orElseThrow(() -> new BusinessException("Job posting not found."));

        if (companyId != null && !job.getCompanyId().equals(companyId)) {
            throw new BusinessException("Unauthorized: This job posting does not belong to your company.");
        }

        TechnicalTest test = TechnicalTest.builder()
                .jobPostingId(request.getJobPostingId())
                .title(request.getTitle())
                .creationMethod(request.getCreationMethod() != null ? request.getCreationMethod() : "MANUAL")
                .problemStatement(request.getProblemStatement())
                .timeLimitMinutes(request.getTimeLimitMinutes() != null ? request.getTimeLimitMinutes() : 45)
                .starterCodeJson(request.getStarterCodeJson() != null ? request.getStarterCodeJson() : "{}")
                .testCasesJson(request.getTestCasesJson() != null ? request.getTestCasesJson() : "[]")
                .build();
        return technicalTestRepository.save(test);
    }

    @Transactional
    public TestAttemptSummaryDto submitTest(UUID jobId, UUID candidateId, SubmitTestRequest request) {
        // BR-13: Decree 13 Consent Mandatory
        if (request.getDecree13ConsentAccepted() == null || !request.getDecree13ConsentAccepted()) {
            throw new BusinessException("Decree 13/2023/ND-CP consent is mandatory to process assessment (BR-13).");
        }

        // BR-15 Attempt Limit check
        if (testAttemptRepository.existsByJobPostingIdAndCandidateId(jobId, candidateId)) {
            throw new BusinessException("Test attempt already exists for this job (BR-15).");
        }

        TechnicalTest test = technicalTestRepository.findByJobPostingId(jobId)
                .orElseThrow(() -> new BusinessException("Technical test not found."));

        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job posting not found."));

        // Sanitize code using regex / mock AI (BR-03)
        String sanitizedCode = aiSanitizerService.sanitizeText(request.getSubmittedCode());

        // MVP Scoring calculation: Simulated automated evaluation
        BigDecimal passRate = BigDecimal.valueOf(88.00);
        BigDecimal cleanCode = BigDecimal.valueOf(92.00);
        BigDecimal totalScore = passRate.multiply(BigDecimal.valueOf(0.6))
                .add(cleanCode.multiply(BigDecimal.valueOf(0.4)))
                .setScale(2, RoundingMode.HALF_UP);

        boolean isPassed = totalScore.compareTo(job.getMinPassScore()) >= 0;

        TestAttempt attempt = TestAttempt.builder()
                .jobPostingId(jobId)
                .technicalTestId(test.getId())
                .candidateId(candidateId)
                .decree13ConsentAccepted(true)
                .consentAcceptedAt(Instant.now())
                .submittedCodeSanitized(sanitizedCode)
                .testcasePassRate(passRate)
                .cleanCodeScore(cleanCode)
                .totalScore(totalScore)
                .isPassedThreshold(isPassed)
                .aiCodeFlagged(false)
                .isRetakeAttempt(false)
                .submittedAt(Instant.now())
                .build();
        attempt = testAttemptRepository.save(attempt);

        // Proctoring log with 90 days retention (BR-12, BR-13)
        ProctoringLog log = ProctoringLog.builder()
                .attemptId(attempt.getId())
                .fullscreenExitCount(request.getFullscreenExitCount() != null ? request.getFullscreenExitCount() : 0)
                .tabSwitchCount(request.getTabSwitchCount() != null ? request.getTabSwitchCount() : 0)
                .keystrokeHistoryJson(request.getKeystrokeHistoryJson() != null ? request.getKeystrokeHistoryJson() : "[]")
                .commitHistoryJson(request.getCommitHistoryJson() != null ? request.getCommitHistoryJson() : "[]")
                .retentionExpiresAt(Instant.now().plus(90, ChronoUnit.DAYS))
                .build();
        proctoringLogRepository.save(log);

        return TestAttemptSummaryDto.builder()
                .attemptId(attempt.getId())
                .jobPostingId(jobId)
                .testcasePassRate(attempt.getTestcasePassRate())
                .cleanCodeScore(attempt.getCleanCodeScore())
                .totalScore(attempt.getTotalScore())
                .isPassedThreshold(attempt.getIsPassedThreshold())
                .aiCodeFlagged(attempt.getAiCodeFlagged())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}
