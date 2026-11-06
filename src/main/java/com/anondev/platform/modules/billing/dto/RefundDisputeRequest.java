package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDisputeRequest {
    @NotNull
    private UUID invitationId;
    @NotBlank
    private String refundReason; // INVALID_CONTACT_INFO, CANDIDATE_NO_SHOW_5_DAYS, CONFIRMED_CHEATING
    private List<String> evidenceUrls;
}
