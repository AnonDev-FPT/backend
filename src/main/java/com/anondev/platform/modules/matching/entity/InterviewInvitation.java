package com.anondev.platform.modules.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_invitations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_posting_id", "candidate_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_posting_id", nullable = false)
    private UUID jobPostingId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "test_attempt_id", nullable = false)
    private UUID testAttemptId;

    @Column(name = "offered_salary_range", length = 100)
    private String offeredSalaryRange;

    @Column(name = "invitation_message_sanitized", columnDefinition = "TEXT")
    private String invitationMessageSanitized;

    @Column(name = "expiry_hours", nullable = false)
    private Integer expiryHours;

    @CreationTimestamp
    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "invitation_status", length = 30)
    @Builder.Default
    private String invitationStatus = "PENDING"; // PENDING, ACCEPTED_UNLOCKED, REJECTED, EXPIRED, REVOKED_BY_COMPANY

    @Column(name = "billing_method", nullable = false, length = 20)
    private String billingMethod; // MONTHLY_QUOTA, WALLET_CREDIT

    @Column(name = "unlock_fee_charged", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unlockFeeCharged = BigDecimal.ZERO;

    @Column(name = "is_billed")
    @Builder.Default
    private Boolean isBilled = false;

    @Column(name = "post_unlock_hiring_status", length = 30)
    @Builder.Default
    private String postUnlockHiringStatus = "AWAITING_UPDATE";

    @Column(name = "status_update_deadline")
    private Instant statusUpdateDeadline;

    @Column(name = "status_updated_at")
    private Instant statusUpdatedAt;
}
