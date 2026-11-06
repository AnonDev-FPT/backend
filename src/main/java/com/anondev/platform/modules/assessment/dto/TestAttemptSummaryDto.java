package com.anondev.platform.modules.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptSummaryDto {
    private UUID attemptId;
    private UUID jobPostingId;
    private BigDecimal testcasePassRate;
    private BigDecimal cleanCodeScore;
    private BigDecimal totalScore;
    private Boolean isPassedThreshold;
    private Boolean aiCodeFlagged;
    private Instant submittedAt;
}
