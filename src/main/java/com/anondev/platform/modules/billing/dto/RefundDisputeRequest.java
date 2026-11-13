package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDisputeRequest {

    @NotNull(message = "Invitation ID is required")
    private UUID invitationId;

    @NotBlank(message = "Refund reason is required (INVALID_CONTACT_INFO, CANDIDATE_NO_SHOW_5_DAYS, CONFIRMED_CHEATING)")
    @Pattern(regexp = "^(INVALID_CONTACT_INFO|CANDIDATE_NO_SHOW_5_DAYS|CONFIRMED_CHEATING)$",
            message = "Reason must be INVALID_CONTACT_INFO, CANDIDATE_NO_SHOW_5_DAYS, or CONFIRMED_CHEATING")
    private String refundReason;

    @Size(max = 10, message = "Maximum 10 evidence URLs allowed")
    private List<@Size(max = 500, message = "Evidence URL cannot exceed 500 characters") String> evidenceUrls;
}
