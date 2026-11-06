package com.anondev.platform.modules.matching.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInvitationRequest {
    @NotNull
    private UUID jobPostingId;
    @NotNull
    private UUID candidateId;
    @NotNull
    private UUID testAttemptId;
    private String offeredSalaryRange;
    private String invitationMessage;
    @Min(24)
    @Max(72)
    @Builder.Default
    private Integer expiryHours = 48;
    @NotBlank
    private String billingMethod; // MONTHLY_QUOTA, WALLET_CREDIT
}
