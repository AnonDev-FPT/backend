package com.anondev.platform.modules.matching.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.assessment.entity.TestAttempt;
import com.anondev.platform.modules.assessment.repository.TestAttemptRepository;
import com.anondev.platform.modules.billing.entity.MonthlyQuotaLedger;
import com.anondev.platform.modules.billing.entity.WalletTransaction;
import com.anondev.platform.modules.billing.repository.MonthlyQuotaLedgerRepository;
import com.anondev.platform.modules.billing.repository.WalletTransactionRepository;
import com.anondev.platform.modules.candidate.dto.IdentityVaultDto;
import com.anondev.platform.modules.candidate.entity.CandidateBlindProfile;
import com.anondev.platform.modules.candidate.entity.CandidateIdentityVault;
import com.anondev.platform.modules.candidate.repository.BlindProfileRepository;
import com.anondev.platform.modules.candidate.repository.IdentityVaultRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.job.entity.JobPosting;
import com.anondev.platform.modules.job.repository.JobPostingRepository;
import com.anondev.platform.modules.matching.dto.*;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import com.anondev.platform.modules.matching.repository.InterviewInvitationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingUnlockService {

    private final InterviewInvitationRepository invitationRepository;
    private final CompanyRepository companyRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final BlindProfileRepository blindProfileRepository;
    private final IdentityVaultRepository identityVaultRepository;
    private final JobPostingRepository jobPostingRepository;
    private final MonthlyQuotaLedgerRepository quotaLedgerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AiSanitizerService aiSanitizerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Integer> parseJsonMap(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Transactional(readOnly = true)
    public List<BlindCandidateCardDto> getQualifiedCandidatesForJob(UUID jobId, UUID companyId) {
        List<TestAttempt> passedAttempts = testAttemptRepository.findByJobPostingIdAndIsPassedThreshold(jobId, true);
        if (passedAttempts.isEmpty()) {
            return List.of();
        }

        List<BlindCandidateCardDto> cards = new ArrayList<>();
        for (TestAttempt attempt : passedAttempts) {
            CandidateBlindProfile blind = blindProfileRepository.findById(attempt.getCandidateId()).orElse(null);
            if (blind == null) continue;

            Optional<InterviewInvitation> optInv = invitationRepository.findByJobPostingIdAndCandidateId(jobId, attempt.getCandidateId());
            String invStatus = optInv.map(InterviewInvitation::getInvitationStatus).orElse("NONE");
            boolean isUnlocked = optInv.map(inv -> "ACCEPTED_UNLOCKED".equalsIgnoreCase(inv.getInvitationStatus())).orElse(false);

            cards.add(BlindCandidateCardDto.builder()
                    .candidateId(attempt.getCandidateId())
                    .candidateCode(blind.getCandidateCode())
                    .provinceCity(blind.getProvinceCity())
                    .workMode(blind.getWorkMode())
                    .experienceLevel(blind.getExperienceLevel())
                    .expectedSalaryMin(blind.getExpectedSalaryMin())
                    .expectedSalaryMax(blind.getExpectedSalaryMax())
                    .primaryTechStack(parseJsonList(blind.getPrimaryTechStack()))
                    .testScore(attempt.getTotalScore())
                    .aiCodeFlagged(attempt.getAiCodeFlagged())
                    .invitationStatus(invStatus)
                    .isUnlocked(isUnlocked)
                    .build());
        }
        return cards;
    }

    @Transactional
    public InterviewInvitation sendInvitation(UUID companyId, SendInvitationRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Company not found"));

        if (company.getIsTaxVerified() == null || !company.getIsTaxVerified()) {
            throw new BusinessException("Company Tax ID (KYB) must be verified before inviting candidates (BR-17).");
        }
        if (company.getIsBlockedByOverdueStatus() != null && company.getIsBlockedByOverdueStatus()) {
            throw new BusinessException("Invitation blocked: Company has overdue candidate status updates (BR-09).");
        }

        // Prevent duplicate pending or accepted invitations (BR-06)
        Optional<InterviewInvitation> existing = invitationRepository.findByJobPostingIdAndCandidateId(
                request.getJobPostingId(), request.getCandidateId());
        if (existing.isPresent()) {
            InterviewInvitation prev = existing.get();
            if ("PENDING".equalsIgnoreCase(prev.getInvitationStatus()) || "ACCEPTED_UNLOCKED".equalsIgnoreCase(prev.getInvitationStatus())) {
                throw new BusinessException("An active invitation already exists for this candidate on this job posting (BR-06).");
            }
        }

        // Hold quota or wallet credit (BR-06, BR-25)
        String billingMethod = request.getBillingMethod() != null ? request.getBillingMethod().toUpperCase() : "WALLET_CREDIT";
        BigDecimal unlockFee = BigDecimal.valueOf(100000); // 100,000 VND

        if ("MONTHLY_QUOTA".equalsIgnoreCase(billingMethod)) {
            LocalDate now = LocalDate.now();
            MonthlyQuotaLedger ledger = quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonth(
                    companyId, now.getYear(), now.getMonthValue()).orElse(null);

            if (ledger == null || (ledger.getAllocatedQuota() + ledger.getRewardedQuota() + ledger.getRefundedQuota() - ledger.getUsedQuota() - ledger.getHeldQuota()) <= 0) {
                throw new BusinessException("Insufficient monthly quota available. Please use credit wallet or subscribe to a higher plan (BR-06).");
            }
            ledger.setHeldQuota(ledger.getHeldQuota() + 1);
            quotaLedgerRepository.save(ledger);
        } else {
            // WALLET_CREDIT
            BigDecimal credit = company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO;
            BigDecimal held = company.getHeldCreditBalance() != null ? company.getHeldCreditBalance() : BigDecimal.ZERO;
            BigDecimal availableCredit = credit.subtract(held);

            if (availableCredit.compareTo(unlockFee) < 0) {
                throw new BusinessException("Insufficient wallet balance. 100.000 VND hold required (BR-06, BR-25). Current available: " + availableCredit + " VND");
            }
            company.setHeldCreditBalance(held.add(unlockFee));
            companyRepository.save(company);
        }

        // Sanitize invitation message (BR-03)
        String sanitizedMsg = aiSanitizerService.sanitizeText(request.getInvitationMessage());

        int hours = (request.getExpiryHours() != null && request.getExpiryHours() >= 24 && request.getExpiryHours() <= 72)
                ? request.getExpiryHours() : 48;
        Instant expiresAt = Instant.now().plus(hours, ChronoUnit.HOURS);

        InterviewInvitation invitation = InterviewInvitation.builder()
                .jobPostingId(request.getJobPostingId())
                .companyId(companyId)
                .candidateId(request.getCandidateId())
                .testAttemptId(request.getTestAttemptId())
                .offeredSalaryRange(request.getOfferedSalaryRange())
                .invitationMessageSanitized(sanitizedMsg)
                .expiryHours(hours)
                .expiresAt(expiresAt)
                .invitationStatus("PENDING")
                .billingMethod(billingMethod)
                .unlockFeeCharged(unlockFee)
                .isBilled(false)
                .postUnlockHiringStatus("AWAITING_UPDATE")
                .build();

        return invitationRepository.save(invitation);
    }

    @Transactional
    public InterviewInvitation respondInvitation(UUID candidateId, UUID invitationId, RespondInvitationRequest request) {
        InterviewInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found"));

        if (!invitation.getCandidateId().equals(candidateId)) {
            throw new BusinessException("Unauthorized: This invitation does not belong to your candidate account.");
        }

        if (!"PENDING".equalsIgnoreCase(invitation.getInvitationStatus())) {
            throw new BusinessException("Invitation is already finalized with status: " + invitation.getInvitationStatus());
        }

        if (Instant.now().isAfter(invitation.getExpiresAt())) {
            invitation.setInvitationStatus("EXPIRED");
            releaseHold(invitation);
            invitationRepository.save(invitation);
            throw new BusinessException("Invitation has expired (BR-06).");
        }

        String action = request.getAction() != null ? request.getAction().toUpperCase() : "";

        if ("ACCEPT".equals(action)) {
            invitation.setInvitationStatus("ACCEPTED_UNLOCKED");
            invitation.setRespondedAt(Instant.now());
            invitation.setIsBilled(true);
            invitation.setStatusUpdateDeadline(Instant.now().plus(14, ChronoUnit.DAYS)); // BR-09: 14 days

            // Finalize billing capture
            finalizeCapture(invitation);

            return invitationRepository.save(invitation);
        } else if ("REJECT".equals(action)) {
            invitation.setInvitationStatus("REJECTED");
            invitation.setRespondedAt(Instant.now());

            // Release hold without charge (0 VND cost for employer - BR-06)
            releaseHold(invitation);

            return invitationRepository.save(invitation);
        } else {
            throw new BusinessException("Invalid action: must be ACCEPT or REJECT");
        }
    }

    private void releaseHold(InterviewInvitation invitation) {
        if ("MONTHLY_QUOTA".equalsIgnoreCase(invitation.getBillingMethod())) {
            LocalDate now = LocalDate.now();
            quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonth(
                    invitation.getCompanyId(), now.getYear(), now.getMonthValue())
                    .ifPresent(ledger -> {
                        ledger.setHeldQuota(Math.max(0, ledger.getHeldQuota() - 1));
                        quotaLedgerRepository.save(ledger);
                    });
        } else {
            companyRepository.findById(invitation.getCompanyId()).ifPresent(company -> {
                BigDecimal unlockFee = invitation.getUnlockFeeCharged() != null ? invitation.getUnlockFeeCharged() : BigDecimal.valueOf(100000);
                BigDecimal held = company.getHeldCreditBalance() != null ? company.getHeldCreditBalance() : BigDecimal.ZERO;
                company.setHeldCreditBalance(held.subtract(unlockFee).max(BigDecimal.ZERO));
                companyRepository.save(company);
            });
        }
    }

    private void finalizeCapture(InterviewInvitation invitation) {
        if ("MONTHLY_QUOTA".equalsIgnoreCase(invitation.getBillingMethod())) {
            LocalDate now = LocalDate.now();
            quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonth(
                    invitation.getCompanyId(), now.getYear(), now.getMonthValue())
                    .ifPresent(ledger -> {
                        ledger.setHeldQuota(Math.max(0, ledger.getHeldQuota() - 1));
                        ledger.setUsedQuota(ledger.getUsedQuota() + 1);
                        quotaLedgerRepository.save(ledger);
                    });
        } else {
            companyRepository.findById(invitation.getCompanyId()).ifPresent(company -> {
                BigDecimal unlockFee = invitation.getUnlockFeeCharged() != null ? invitation.getUnlockFeeCharged() : BigDecimal.valueOf(100000);
                BigDecimal held = company.getHeldCreditBalance() != null ? company.getHeldCreditBalance() : BigDecimal.ZERO;
                BigDecimal credit = company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO;

                company.setHeldCreditBalance(held.subtract(unlockFee).max(BigDecimal.ZERO));
                company.setCreditBalance(credit.subtract(unlockFee).max(BigDecimal.ZERO));
                companyRepository.save(company);

                walletTransactionRepository.save(WalletTransaction.builder()
                        .companyId(company.getId())
                        .transactionType("INVITATION_UNLOCK_FEE")
                        .amount(unlockFee)
                        .paymentGateway("INTERNAL_WALLET")
                        .referenceId(invitation.getId())
                        .description("2-Way Mutual Handshake Unlock Fee for Candidate Invitation " + invitation.getId())
                        .build());
            });
        }
    }

    @Transactional(readOnly = true)
    public UnlockedCandidateFullDto getUnlockedProfile(UUID companyId, UUID invitationId) {
        InterviewInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found"));

        if (!invitation.getCompanyId().equals(companyId)) {
            throw new BusinessException("Unauthorized: Invitation does not belong to your company.");
        }

        // BR-02: Only unlocked when ACCEPTED_UNLOCKED and is_billed is true
        if (!"ACCEPTED_UNLOCKED".equalsIgnoreCase(invitation.getInvitationStatus()) || !Boolean.TRUE.equals(invitation.getIsBilled())) {
            throw new BusinessException("Candidate identity vault remains locked. Status must be ACCEPTED_UNLOCKED (BR-02). Current status: " + invitation.getInvitationStatus());
        }

        CandidateIdentityVault vault = identityVaultRepository.findById(invitation.getCandidateId())
                .orElseThrow(() -> new BusinessException("Candidate vault not found"));

        CandidateBlindProfile blind = blindProfileRepository.findById(invitation.getCandidateId())
                .orElseThrow(() -> new BusinessException("Candidate blind profile not found"));

        TestAttempt attempt = testAttemptRepository.findById(invitation.getTestAttemptId())
                .orElse(null);

        IdentityVaultDto vaultDto = IdentityVaultDto.builder()
                .realFullName(vault.getRealFullName())
                .contactEmail(vault.getContactEmail())
                .contactPhone(vault.getContactPhone())
                .universityName(vault.getUniversityName())
                .gender(vault.getGender())
                .avatarUrl(vault.getAvatarUrl())
                .originalCvUrl(vault.getOriginalCvUrl())
                .sanitizedCvUrl(vault.getSanitizedCvUrl())
                .portfolioLinks(parseJsonList(vault.getPortfolioLinks()))
                .build();

        return UnlockedCandidateFullDto.builder()
                .invitationId(invitation.getId())
                .candidateCode(blind.getCandidateCode())
                .invitationStatus(invitation.getInvitationStatus())
                .isBilled(invitation.getIsBilled())
                .billingMethodUsed(invitation.getBillingMethod())
                .skillRadarScores(parseJsonMap(blind.getSkillRadarScores()))
                .submittedCodeSanitized(attempt != null ? attempt.getSubmittedCodeSanitized() : null)
                .identityVault(vaultDto)
                .postUnlockHiringStatus(invitation.getPostUnlockHiringStatus())
                .statusUpdateDeadline(invitation.getStatusUpdateDeadline())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CandidateInvitationItemDto> getCandidateInvitations(UUID candidateId) {
        List<InterviewInvitation> invitations = invitationRepository.findByCandidateId(candidateId);
        List<CandidateInvitationItemDto> items = new ArrayList<>();
        for (InterviewInvitation inv : invitations) {
            JobPosting job = jobPostingRepository.findById(inv.getJobPostingId()).orElse(null);
            Company comp = companyRepository.findById(inv.getCompanyId()).orElse(null);
            items.add(CandidateInvitationItemDto.builder()
                    .id(inv.getId())
                    .jobPostingId(inv.getJobPostingId())
                    .jobTitle(job != null ? job.getTitle() : "Vị trí tuyển dụng")
                    .companyName(comp != null ? comp.getCompanyName() : "Doanh nghiệp ẩn danh")
                    .offeredSalaryRange(inv.getOfferedSalaryRange())
                    .invitationMessage(inv.getInvitationMessageSanitized())
                    .invitationStatus(inv.getInvitationStatus())
                    .expiresAt(inv.getExpiresAt())
                    .sentAt(inv.getSentAt())
                    .build());
        }
        return items;
    }

    @Transactional
    public void updateHiringStatus(UUID companyId, UUID invitationId, String status) {
        InterviewInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found"));

        if (!invitation.getCompanyId().equals(companyId)) {
            throw new BusinessException("Unauthorized: Invitation does not belong to your company.");
        }

        if (!"ACCEPTED_UNLOCKED".equalsIgnoreCase(invitation.getInvitationStatus())) {
            throw new BusinessException("Cannot update hiring status: candidate has not accepted unlock (BR-09).");
        }

        String normalizedStatus = status != null ? status.toUpperCase().trim() : "";
        List<String> validStatuses = List.of("AWAITING_UPDATE", "INTERVIEW_SCHEDULED", "HIRED", "REJECTED");
        if (!validStatuses.contains(normalizedStatus)) {
            throw new BusinessException("Invalid hiring status: " + status + ". Allowed values: " + validStatuses + " (BR-09)");
        }

        invitation.setPostUnlockHiringStatus(normalizedStatus);
        invitation.setStatusUpdatedAt(Instant.now());
        invitationRepository.save(invitation);

        // Update company reward counter (BR-09: Every 5 updates gives +1 free quota)
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company != null) {
            int count = (company.getStatusUpdateCounter() != null ? company.getStatusUpdateCounter() : 0) + 1;
            company.setStatusUpdateCounter(count);
            if (count % 5 == 0) {
                LocalDate now = LocalDate.now();
                quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonth(
                        companyId, now.getYear(), now.getMonthValue())
                        .ifPresent(ledger -> {
                            ledger.setRewardedQuota(ledger.getRewardedQuota() + 1);
                            quotaLedgerRepository.save(ledger);
                        });
            }
            companyRepository.save(company);
        }
    }
}
