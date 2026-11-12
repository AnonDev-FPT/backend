package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.TournamentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentSubmissionRepository extends JpaRepository<TournamentSubmission, UUID> {
    List<TournamentSubmission> findByTournamentIdAndCandidateIdOrderBySubmittedAtDesc(UUID tournamentId, UUID candidateId);
    Optional<TournamentSubmission> findTopByTournamentIdAndProblemIdAndCandidateIdAndVerdictOrderByScoreAwardedDesc(
            UUID tournamentId, UUID problemId, UUID candidateId, String verdict);
    long countByTournamentIdAndCandidateIdAndVerdict(UUID tournamentId, UUID candidateId, String verdict);
}

