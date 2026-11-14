package com.anondev.platform.modules.matching.service;

import com.anondev.platform.modules.matching.dto.BlindCandidateCardDto;
import com.anondev.platform.modules.matching.dto.CandidateInvitationItemDto;
import com.anondev.platform.modules.matching.dto.InterviewInvitationResponse;
import com.anondev.platform.modules.matching.dto.RespondInvitationRequest;
import com.anondev.platform.modules.matching.dto.SendInvitationRequest;
import com.anondev.platform.modules.matching.dto.UnlockedCandidateFullDto;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;

import java.util.List;
import java.util.UUID;

public interface MatchingUnlockService {

    List<BlindCandidateCardDto> getQualifiedCandidatesForJob(String userEmail, UUID jobId);

    InterviewInvitationResponse sendInvitationByUserEmail(String userEmail, SendInvitationRequest request);

    List<CandidateInvitationItemDto> getCandidateInvitationsByUserEmail(String userEmail);

    InterviewInvitationResponse respondInvitationByUserEmail(String userEmail, UUID invitationId, RespondInvitationRequest request);

    UnlockedCandidateFullDto getUnlockedProfileByUserEmail(String userEmail, UUID invitationId);

    void updateHiringStatusByUserEmail(String userEmail, UUID invitationId, String status);

    List<BlindCandidateCardDto> getQualifiedCandidatesForJob(UUID jobId, UUID companyId);

    InterviewInvitation sendInvitation(UUID companyId, SendInvitationRequest request);

    InterviewInvitation respondInvitation(UUID candidateId, UUID invitationId, RespondInvitationRequest request);

    void expireInvitation(InterviewInvitation invitation);

    void releaseHold(InterviewInvitation invitation);

    UnlockedCandidateFullDto getUnlockedProfile(UUID companyId, UUID invitationId);

    List<CandidateInvitationItemDto> getCandidateInvitations(UUID candidateId);

    void updateHiringStatus(UUID companyId, UUID invitationId, String status);
}
