package com.anondev.platform.modules.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlindProfileResponseDto {
    private String candidateCode;
    private String provinceCity;
    private String workMode;
    private String experienceLevel;
    private BigDecimal expectedSalaryMin;
    private BigDecimal expectedSalaryMax;
    private List<String> primaryTechStack;
    private Object skillRadarScores;
    private String sanitizedBio;
    private BigDecimal overallRankingScore;
}
