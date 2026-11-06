package com.anondev.platform.modules.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestRequest {

    @NotNull(message = "Job posting ID is required")
    private UUID jobPostingId;

    @NotBlank(message = "Title is required")
    private String title;

    private String creationMethod; // MANUAL, AI_FROM_JD

    @NotBlank(message = "Problem statement is required")
    private String problemStatement;

    private Integer timeLimitMinutes;
    private String starterCodeJson;
    private String testCasesJson;
}
