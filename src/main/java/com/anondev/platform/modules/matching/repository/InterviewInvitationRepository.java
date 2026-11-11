package com.anondev.platform.modules.matching.repository;

import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT i FROM InterviewInvitation i WHERE i.invitationStatus = 'ACCEPTED_UNLOCKED' AND i.postUnlockHiringStatus = 'AWAITING_UPDATE' AND i.statusUpdateDeadline IS NOT NULL AND i.statusUpdateDeadline < :now")
    List<InterviewInvitation> findOverdueStatusInvitations(@Param("now") Instant now);
}
