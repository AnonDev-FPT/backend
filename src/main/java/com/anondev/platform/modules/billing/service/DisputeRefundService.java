package com.anondev.platform.modules.billing.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.billing.dto.RefundDisputeRequest;
import com.anondev.platform.modules.billing.entity.MonthlyQuotaLedger;
import com.anondev.platform.modules.billing.entity.RefundDispute;
import com.anondev.platform.modules.billing.entity.WalletTransaction;
import com.anondev.platform.modules.billing.repository.MonthlyQuotaLedgerRepository;
import com.anondev.platform.modules.billing.repository.RefundDisputeRepository;
import com.anondev.platform.modules.billing.repository.WalletTransactionRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import com.anondev.platform.modules.matching.repository.InterviewInvitationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeRefundService {

    private final RefundDisputeRepository refundDisputeRepository;
    private final InterviewInvitationRepository invitationRepository;
    private final CompanyRepository companyRepository;
    private final MonthlyQuotaLedgerRepository quotaLedgerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public RefundDispute createDisputeByUserEmail(String userEmail, RefundDisputeRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for current user"));
        return createDispute(company.getId(), request);
    }

    @Transactional
    public void resolveDisputeByUserEmail(String adminEmail, UUID disputeId, boolean approve) {
        UUID adminId = null;
        if (adminEmail != null) {
            adminId = userRepository.findByEmail(adminEmail)
                    .map(User::getId)
                    .orElse(null);
        }
        resolveDispute(adminId, disputeId, approve);
    }

    private static final List<String> VALID_REASONS = List.of(
            "INVALID_CONTACT_INFO", "CANDIDATE_NO_SHOW_5_DAYS", "CONFIRMED_CHEATING"
    );

    @Transactional
    public RefundDispute createDispute(UUID companyId, RefundDisputeRequest request) {
        String reason = request.getRefundReason() != null ? request.getRefundReason().trim().toUpperCase() : "";
        if (!VALID_REASONS.contains(reason)) {
            throw new BusinessException("Invalid refund reason: " + request.getRefundReason() + ". Allowed: " + VALID_REASONS + " (BR-18)");
        }

        InterviewInvitation invitation = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new BusinessException("Invitation not found."));

        if (!invitation.getCompanyId().equals(companyId)) {
            throw new BusinessException("Unauthorized: This invitation does not belong to your company.");
        }

        if (!"ACCEPTED_UNLOCKED".equalsIgnoreCase(invitation.getInvitationStatus()) || !Boolean.TRUE.equals(invitation.getIsBilled())) {
            throw new BusinessException("Refund dispute can only be filed for unlocked and billed candidates (BR-18).");
        }

        // BR-18: Must be submitted within 7 days of unlock
        Instant unlockTime = invitation.getRespondedAt() != null ? invitation.getRespondedAt() : invitation.getSentAt();
        if (unlockTime.isBefore(Instant.now().minus(7, ChronoUnit.DAYS))) {
            throw new BusinessException("Dispute window expired: Refund disputes must be submitted within 7 days of unlock (BR-18, BR-20).");
        }

        if (refundDisputeRepository.findByInvitationId(invitation.getId()).isPresent()) {
            throw new BusinessException("A refund dispute has already been submitted for this invitation.");
        }

        String evidenceJson = "[]";
        if (request.getEvidenceUrls() != null && !request.getEvidenceUrls().isEmpty()) {
            try {
                evidenceJson = objectMapper.writeValueAsString(request.getEvidenceUrls());
            } catch (Exception e) {
                log.warn("Failed to serialize evidence URLs to JSON: {}", e.getMessage());
            }
        }

        RefundDispute dispute = RefundDispute.builder()
                .invitationId(invitation.getId())
                .companyId(companyId)
                .refundReason(reason)
                .evidenceUrlsJson(evidenceJson)
                .disputeStatus("PENDING")
                .resolutionDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();

        return refundDisputeRepository.save(dispute);
    }

    @Transactional
    public void resolveDispute(UUID adminId, UUID disputeId, boolean approve) {
        RefundDispute dispute = refundDisputeRepository.findByIdForUpdate(disputeId)
                .orElseThrow(() -> new BusinessException("Dispute not found."));

        if (!"PENDING".equalsIgnoreCase(dispute.getDisputeStatus())) {
            throw new BusinessException("Dispute has already been resolved with status: " + dispute.getDisputeStatus());
        }

        InterviewInvitation invitation = invitationRepository.findById(dispute.getInvitationId())
                .orElseThrow(() -> new BusinessException("Associated invitation not found."));

        dispute.setResolvedByAdminId(adminId);
        dispute.setResolvedAt(Instant.now());

        if (approve) {
            dispute.setDisputeStatus("APPROVED_REFUNDED");
            invitation.setPostUnlockHiringStatus("DISPUTE_REFUNDED");
            invitationRepository.save(invitation);

            if ("MONTHLY_QUOTA".equalsIgnoreCase(invitation.getBillingMethod())) {
                dispute.setRefundDestination("MONTHLY_QUOTA");
                LocalDate now = LocalDate.now();
                MonthlyQuotaLedger ledger = quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonthForUpdate(
                        dispute.getCompanyId(), now.getYear(), now.getMonthValue()
                ).orElseGet(() -> MonthlyQuotaLedger.builder()
                        .companyId(dispute.getCompanyId())
                        .cycleYear(now.getYear())
                        .cycleMonth(now.getMonthValue())
                        .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                        .build());

                ledger.setRefundedQuota(ledger.getRefundedQuota() + 1);
                quotaLedgerRepository.save(ledger);
                log.info("Dispute {} approved: +1 refunded quota credited to company {}.", dispute.getId(), dispute.getCompanyId());
            } else {
                dispute.setRefundDestination("WALLET_CREDIT");
                Company company = companyRepository.findByIdForUpdate(dispute.getCompanyId())
                        .orElseThrow(() -> new BusinessException("Company not found."));

                BigDecimal refundAmount = invitation.getUnlockFeeCharged() != null
                        ? invitation.getUnlockFeeCharged()
                        : BigDecimal.valueOf(100000);

                BigDecimal newBalance = (company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO).add(refundAmount);
                company.setCreditBalance(newBalance);
                companyRepository.save(company);

                walletTransactionRepository.save(WalletTransaction.builder()
                        .companyId(company.getId())
                        .transactionType("DISPUTE_REFUND_CREDIT")
                        .amount(refundAmount)
                        .paymentGateway("INTERNAL_WALLET")
                        .referenceId(dispute.getId())
                        .description("Refund approved by Admin for Dispute #" + dispute.getId() + " (" + dispute.getRefundReason() + ")")
                        .build());

                log.info("Dispute {} approved: {} VND refunded to company {} wallet.", dispute.getId(), refundAmount, company.getId());
            }
        } else {
            dispute.setDisputeStatus("REJECTED");
            log.info("Dispute {} rejected by admin {}.", dispute.getId(), adminId);
        }

        refundDisputeRepository.save(dispute);
    }
}
