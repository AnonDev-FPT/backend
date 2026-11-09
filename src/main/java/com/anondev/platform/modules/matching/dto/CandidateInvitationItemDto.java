package com.anondev.platform.modules.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateInvitationItemDto {
    private UUID id;
    private UUID jobPostingId;
    private String jobTitle;
    private String companyName;
    private String offeredSalaryRange;
    private String invitationMessage;
    private String invitationStatus;
    private Instant expiresAt;
    private Instant sentAt;
}

