package com.anondev.platform.modules.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "refund_disputes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDispute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invitation_id", nullable = false, unique = true)
    private UUID invitationId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "refund_reason", nullable = false, length = 40)
    private String refundReason; // INVALID_CONTACT_INFO, CANDIDATE_NO_SHOW_5_DAYS, CONFIRMED_CHEATING

    @Column(name = "evidence_urls_json", columnDefinition = "JSONB")
    @Builder.Default
    private String evidenceUrlsJson = "[]";

    @CreationTimestamp
    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "resolution_deadline", nullable = false)
    @Builder.Default
    private Instant resolutionDeadline = Instant.now().plus(5, ChronoUnit.DAYS);

    @Column(name = "dispute_status", length = 20)
    @Builder.Default
    private String disputeStatus = "PENDING"; // PENDING, APPROVED_REFUNDED, REJECTED

    @Column(name = "refund_destination", length = 20)
    private String refundDestination; // WALLET_CREDIT, MONTHLY_QUOTA

    @Column(name = "resolved_by_admin_id")
    private UUID resolvedByAdminId;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
