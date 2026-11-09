package com.anondev.platform.modules.matching.repository;

import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewInvitationRepository extends JpaRepository<InterviewInvitation, UUID> {
    Optional<InterviewInvitation> findByJobPostingIdAndCandidateId(UUID jobPostingId, UUID candidateId);
    List<InterviewInvitation> findByCandidateId(UUID candidateId);
    List<InterviewInvitation> findByCompanyId(UUID companyId);
    List<InterviewInvitation> findByInvitationStatusAndExpiresAtBefore(String status, Instant now);
}
