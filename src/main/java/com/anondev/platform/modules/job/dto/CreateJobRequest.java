package com.anondev.platform.modules.job.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Province / City is required")
    private String provinceCity;

    @NotBlank(message = "Work mode is required (REMOTE, OFFLINE, HYBRID)")
    private String workMode;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @NotNull(message = "Minimum pass score is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minPassScore;

    @NotBlank(message = "Funding source is required (FREE_TRIAL, SINGLE_PURCHASE, SUBSCRIPTION_SLOT)")
    private String fundingSource;

    // Technical Test configuration to accompany the job (BR-11)
    private String testTitle;
    private String problemStatement;
    private Integer timeLimitMinutes;
    private String starterCodeJson;
    private String testCasesJson;
}
