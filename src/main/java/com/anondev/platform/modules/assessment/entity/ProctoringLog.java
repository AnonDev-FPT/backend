package com.anondev.platform.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "proctoring_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attempt_id", nullable = false, unique = true)
    private UUID attemptId;

    @Column(name = "fullscreen_exit_count")
    @Builder.Default
    private Integer fullscreenExitCount = 0;

    @Column(name = "tab_switch_count")
    @Builder.Default
    private Integer tabSwitchCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keystroke_history_json", columnDefinition = "JSONB")
    @Builder.Default
    private String keystrokeHistoryJson = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "commit_history_json", columnDefinition = "JSONB")
    @Builder.Default
    private String commitHistoryJson = "[]";

    @Column(name = "ai_detection_confidence", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aiDetectionConfidence = BigDecimal.ZERO;

    @Column(name = "is_anonymized_after_expiry")
    @Builder.Default
    private Boolean isAnonymizedAfterExpiry = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "retention_expires_at", nullable = false)
    @Builder.Default
    private Instant retentionExpiresAt = Instant.now().plus(90, ChronoUnit.DAYS);
}
