package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, UUID> {
    Optional<TournamentParticipant> findByTournamentIdAndCandidateId(UUID tournamentId, UUID candidateId);
    boolean existsByTournamentIdAndCandidateId(UUID tournamentId, UUID candidateId);
    long countByTournamentId(UUID tournamentId);

    @Query("SELECT p FROM TournamentParticipant p WHERE p.tournamentId = :tournamentId AND p.isDisqualified = false ORDER BY p.totalScore DESC, p.penaltySeconds ASC")
    List<TournamentParticipant> findLeaderboard(@Param("tournamentId") UUID tournamentId);
}

