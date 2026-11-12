package com.anondev.platform.modules.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTournamentSolutionRequest {

    @NotBlank(message = "Programming language is required")
    @Builder.Default
    private String language = "JAVA";

    @NotBlank(message = "Source code cannot be empty")
    @Size(max = 100000, message = "Source code exceeds maximum allowed size (100KB)")
    private String sourceCode;
}

