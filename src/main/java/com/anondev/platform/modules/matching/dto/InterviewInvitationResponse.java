package com.anondev.platform.modules.matching.dto;

import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewInvitationResponse {
    private UUID id;
    private UUID jobPostingId;
    private UUID companyId;
    private UUID candidateId;
    private UUID testAttemptId;
    private String offeredSalaryRange;
    private String invitationMessageSanitized;
    private Integer expiryHours;
    private Instant sentAt;
    private Instant expiresAt;
    private Instant respondedAt;
    private String invitationStatus;
    private String billingMethod;
    private BigDecimal unlockFeeCharged;
    private Boolean isBilled;
    private String postUnlockHiringStatus;
    private Instant statusUpdateDeadline;
    private Instant statusUpdatedAt;

    public static InterviewInvitationResponse fromEntity(InterviewInvitation entity) {
        if (entity == null) {
            return null;
        }
        return InterviewInvitationResponse.builder()
                .id(entity.getId())
                .jobPostingId(entity.getJobPostingId())
                .companyId(entity.getCompanyId())
                .candidateId(entity.getCandidateId())
                .testAttemptId(entity.getTestAttemptId())
                .offeredSalaryRange(entity.getOfferedSalaryRange())
                .invitationMessageSanitized(entity.getInvitationMessageSanitized())
                .expiryHours(entity.getExpiryHours())
                .sentAt(entity.getSentAt())
                .expiresAt(entity.getExpiresAt())
                .respondedAt(entity.getRespondedAt())
                .invitationStatus(entity.getInvitationStatus())
                .billingMethod(entity.getBillingMethod())
                .unlockFeeCharged(entity.getUnlockFeeCharged())
                .isBilled(entity.getIsBilled())
                .postUnlockHiringStatus(entity.getPostUnlockHiringStatus())
                .statusUpdateDeadline(entity.getStatusUpdateDeadline())
                .statusUpdatedAt(entity.getStatusUpdatedAt())
                .build();
    }
}

