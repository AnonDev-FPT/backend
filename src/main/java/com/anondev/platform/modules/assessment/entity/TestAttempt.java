package com.anondev.platform.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "test_attempts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_posting_id", "candidate_id", "is_retake_attempt"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_posting_id", nullable = false)
    private UUID jobPostingId;

    @Column(name = "technical_test_id", nullable = false)
    private UUID technicalTestId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "decree13_consent_accepted", nullable = false)
    private Boolean decree13ConsentAccepted;

    @Column(name = "consent_accepted_at", nullable = false)
    @Builder.Default
    private Instant consentAcceptedAt = Instant.now();

    @Column(name = "submitted_code_sanitized", columnDefinition = "TEXT")
    private String submittedCodeSanitized;

    @Column(name = "testcase_pass_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal testcasePassRate = BigDecimal.ZERO;

    @Column(name = "clean_code_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cleanCodeScore = BigDecimal.ZERO;

    @Column(name = "total_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "is_passed_threshold")
    @Builder.Default
    private Boolean isPassedThreshold = false;

    @Column(name = "ai_code_flagged")
    @Builder.Default
    private Boolean aiCodeFlagged = false;

    @Column(name = "appeal_status", length = 30)
    @Builder.Default
    private String appealStatus = "NONE";

    @Column(name = "appeal_explanation", columnDefinition = "TEXT")
    private String appealExplanation;

    @Column(name = "is_retake_attempt")
    @Builder.Default
    private Boolean isRetakeAttempt = false;

    @CreationTimestamp
    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
