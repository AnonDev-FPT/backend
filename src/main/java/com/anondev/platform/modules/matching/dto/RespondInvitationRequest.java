package com.anondev.platform.modules.matching.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondInvitationRequest {
    @NotBlank
    private String action; // ACCEPT, REJECT
}
