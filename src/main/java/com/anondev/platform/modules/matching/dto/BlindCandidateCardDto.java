package com.anondev.platform.modules.matching.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlindCandidateCardDto {
    private UUID candidateId;
    private String candidateCode;
    private String provinceCity;
    private String workMode;
    private String experienceLevel;
    private BigDecimal expectedSalaryMin;
    private BigDecimal expectedSalaryMax;
    private List<String> primaryTechStack;
    private BigDecimal testScore;
    private Boolean aiCodeFlagged;
    private String invitationStatus;
    private Boolean isUnlocked;
}
