package com.anondev.platform.modules.matching.dto;

import com.anondev.platform.modules.candidate.dto.IdentityVaultDto;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockedCandidateFullDto {
    private UUID invitationId;
    private String candidateCode;
    private String invitationStatus;
    private Boolean isBilled;
    private String billingMethodUsed;
    private Map<String, Integer> skillRadarScores;
    private String submittedCodeSanitized;
    private IdentityVaultDto identityVault;
    private String postUnlockHiringStatus;
    private Instant statusUpdateDeadline;
}
