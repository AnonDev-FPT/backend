package com.anondev.platform.modules.matching.service;

import com.anondev.platform.modules.matching.dto.*;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import com.anondev.platform.modules.matching.repository.InterviewInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchingUnlockService {
    private final InterviewInvitationRepository invitationRepository;

    public List<BlindCandidateCardDto> getQualifiedCandidatesForJob(UUID jobId, UUID companyId) {
        return List.of();
    }

    public InterviewInvitation sendInvitation(UUID companyId, SendInvitationRequest request) {
        return null;
    }

    public InterviewInvitation respondInvitation(UUID candidateId, UUID invitationId, RespondInvitationRequest request) {
        return null;
    }

    public UnlockedCandidateFullDto getUnlockedProfile(UUID companyId, UUID invitationId) {
        return null;
    }

    public void updateHiringStatus(UUID companyId, UUID invitationId, String status) {
    }
}
