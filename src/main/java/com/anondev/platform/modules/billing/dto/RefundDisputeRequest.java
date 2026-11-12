package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    private List<String> evidenceUrls;
}
