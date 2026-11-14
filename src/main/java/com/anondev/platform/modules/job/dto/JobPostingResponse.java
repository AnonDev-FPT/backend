package com.anondev.platform.modules.job.dto;

import com.anondev.platform.modules.job.entity.JobPosting;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingResponse {
    private UUID id;
    private UUID companyId;
    private String title;
    private String descriptionSanitized;
    private String provinceCity;
    private String workMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private BigDecimal minPassScore;
    private String fundingSource;
    private Boolean isPriorityDisplay;
    private Instant publishedAt;
    private Instant expiresAt;
    private String status;

    public static JobPostingResponse fromEntity(JobPosting entity) {
        if (entity == null) {
            return null;
        }
        return JobPostingResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .title(entity.getTitle())
                .descriptionSanitized(entity.getDescriptionSanitized())
                .provinceCity(entity.getProvinceCity())
                .workMode(entity.getWorkMode())
                .salaryMin(entity.getSalaryMin())
                .salaryMax(entity.getSalaryMax())
                .minPassScore(entity.getMinPassScore())
                .fundingSource(entity.getFundingSource())
                .isPriorityDisplay(entity.getIsPriorityDisplay())
                .publishedAt(entity.getPublishedAt())
                .expiresAt(entity.getExpiresAt())
                .status(entity.getStatus())
                .build();
    }
}

