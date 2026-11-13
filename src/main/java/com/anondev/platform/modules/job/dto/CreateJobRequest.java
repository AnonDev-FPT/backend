package com.anondev.platform.modules.job.dto;

import jakarta.validation.constraints.*;
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
    @Size(max = 200, message = "Job title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Province / City is required")
    @Size(max = 100, message = "Province/City cannot exceed 100 characters")
    private String provinceCity;

    @NotBlank(message = "Work mode is required (REMOTE, OFFLINE, HYBRID)")
    @Pattern(regexp = "^(REMOTE|ONSITE|OFFLINE|HYBRID)$", message = "Work mode must be REMOTE, ONSITE, OFFLINE, or HYBRID")
    private String workMode;

    @PositiveOrZero(message = "Salary minimum must be greater than or equal to 0")
    private BigDecimal salaryMin;

    @PositiveOrZero(message = "Salary maximum must be greater than or equal to 0")
    private BigDecimal salaryMax;

    @NotNull(message = "Minimum pass score is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum pass score must be at least 0.0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Minimum pass score cannot exceed 100.0")
    private BigDecimal minPassScore;

    @NotBlank(message = "Funding source is required (FREE_TRIAL, SINGLE_PURCHASE, SUBSCRIPTION_SLOT)")
    @Pattern(regexp = "^(FREE_TRIAL|SINGLE_PURCHASE|SUBSCRIPTION_SLOT)$", message = "Funding source must be FREE_TRIAL, SINGLE_PURCHASE, or SUBSCRIPTION_SLOT")
    private String fundingSource;

    // Technical Test configuration to accompany the job (BR-11)
    @Size(max = 200, message = "Test title cannot exceed 200 characters")
    private String testTitle;

    private String problemStatement;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    @Max(value = 300, message = "Time limit cannot exceed 300 minutes")
    private Integer timeLimitMinutes;

    private String starterCodeJson;
    private String testCasesJson;
}
