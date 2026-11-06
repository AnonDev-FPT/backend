package com.anondev.platform.modules.assessment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
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

    private String submittedCode;
    private Integer fullscreenExitCount;
    private Integer tabSwitchCount;
    private String keystrokeHistoryJson;
    private String commitHistoryJson;
}
