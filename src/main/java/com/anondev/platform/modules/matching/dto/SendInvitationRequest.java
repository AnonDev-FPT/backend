package com.anondev.platform.modules.matching.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInvitationRequest {

    @NotNull(message = "Job posting ID is required")
    private UUID jobPostingId;

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Test attempt ID is required")
    private UUID testAttemptId;

    @Size(max = 100, message = "Offered salary range must not exceed 100 characters")
    private String offeredSalaryRange;

    @Size(max = 1000, message = "Invitation message must not exceed 1000 characters")
    private String invitationMessage;

    @Min(value = 24, message = "Expiry hours must be at least 24 hours")
    @Max(value = 72, message = "Expiry hours cannot exceed 72 hours")
    @Builder.Default
    private Integer expiryHours = 48;

    @NotBlank(message = "Billing method is required (MONTHLY_QUOTA or WALLET_CREDIT)")
    @Pattern(regexp = "^(MONTHLY_QUOTA|WALLET_CREDIT)$", message = "Billing method must be MONTHLY_QUOTA or WALLET_CREDIT")
    private String billingMethod;
}
