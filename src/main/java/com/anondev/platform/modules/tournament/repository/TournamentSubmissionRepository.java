package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.TournamentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentSubmissionRepository extends JpaRepository<TournamentSubmission, UUID> {
    List<TournamentSubmission> findByTournamentIdAndCandidateIdOrderBySubmittedAtDesc(UUID tournamentId, UUID candidateId);
    Optional<TournamentSubmission> findTopByTournamentIdAndProblemIdAndCandidateIdAndVerdictOrderByScoreAwardedDesc(
            UUID tournamentId, UUID problemId, UUID candidateId, String verdict);
    boolean existsByTournamentIdAndProblemIdAndCandidateIdAndVerdict(
            UUID tournamentId, UUID problemId, UUID candidateId, String verdict);
    long countByTournamentIdAndCandidateIdAndVerdict(UUID tournamentId, UUID candidateId, String verdict);

    @Query("SELECT COUNT(DISTINCT s.problemId) FROM TournamentSubmission s WHERE s.tournamentId = :tournamentId AND s.candidateId = :candidateId AND s.verdict = 'ACCEPTED'")
    long countDistinctSolvedProblems(@Param("tournamentId") UUID tournamentId, @Param("candidateId") UUID candidateId);

    long countByTournamentIdAndProblemIdAndCandidateIdAndVerdictNot(
            UUID tournamentId, UUID problemId, UUID candidateId, String verdict);
}


