package com.anondev.platform.modules.assessment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTestRequest {

    @NotNull(message = "Decree 13 consent is mandatory")
    @AssertTrue(message = "You must accept Decree 13/2023/ND-CP consent before submitting")
    private Boolean decree13ConsentAccepted;

    @Size(max = 100000, message = "Submitted code exceeds maximum allowed size (100KB)")
    private String submittedCode;

    @PositiveOrZero(message = "Fullscreen exit count must be >= 0")
    private Integer fullscreenExitCount;

    @PositiveOrZero(message = "Tab switch count must be >= 0")
    private Integer tabSwitchCount;

    private String keystrokeHistoryJson;
    private String commitHistoryJson;
}
