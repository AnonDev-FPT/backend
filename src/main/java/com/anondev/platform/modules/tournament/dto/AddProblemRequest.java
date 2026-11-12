package com.anondev.platform.modules.tournament.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProblemRequest {

    @NotBlank(message = "Problem title is required")
    @Size(max = 255, message = "Problem title cannot exceed 255 characters")
    private String title;

    @Pattern(regexp = "^(EASY|MEDIUM|HARD)$", message = "Difficulty must be EASY, MEDIUM, or HARD")
    @Builder.Default
    private String difficulty = "MEDIUM";

    @NotBlank(message = "Problem statement is required")
    private String problemStatement;

    @Min(value = 10, message = "Points must be at least 10")
    @Max(value = 1000, message = "Points cannot exceed 1000")
    @Builder.Default
    private Integer points = 100;

    @Min(value = 100, message = "Time limit must be at least 100 ms")
    @Max(value = 10000, message = "Time limit cannot exceed 10,000 ms")
    @Builder.Default
    private Integer timeLimitMs = 1000;

    @Min(value = 16000, message = "Memory limit must be at least 16,000 KB")
    @Builder.Default
    private Integer memoryLimitKb = 256000;

    @Builder.Default
    private String starterCodeJson = "{}";

    @Builder.Default
    private String testCasesJson = "[]";
}

