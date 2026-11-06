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
public class UpdateProfileRequest {
    // Identity Vault fields
    private String realFullName;
    private String contactEmail;
    private String contactPhone;
    private String universityName;
    private String gender;
    private String avatarUrl;
    private String originalCvUrl;
    private List<String> portfolioLinks;

    // Blind Profile fields
    private String provinceCity;
    private String workMode;
    private String experienceLevel;
    private BigDecimal expectedSalaryMin;
    private BigDecimal expectedSalaryMax;
    private List<String> primaryTechStack;
    private String bio;
}
