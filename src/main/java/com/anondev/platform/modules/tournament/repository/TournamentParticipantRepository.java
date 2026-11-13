package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.TournamentParticipant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, UUID> {
    Optional<TournamentParticipant> findByTournamentIdAndCandidateId(UUID tournamentId, UUID candidateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM TournamentParticipant p WHERE p.tournamentId = :tournamentId AND p.candidateId = :candidateId")
    Optional<TournamentParticipant> findByTournamentIdAndCandidateIdForUpdate(
            @Param("tournamentId") UUID tournamentId,
            @Param("candidateId") UUID candidateId
    );

    boolean existsByTournamentIdAndCandidateId(UUID tournamentId, UUID candidateId);
    long countByTournamentId(UUID tournamentId);

    @Query("SELECT p FROM TournamentParticipant p WHERE p.tournamentId = :tournamentId AND p.isDisqualified = false ORDER BY p.totalScore DESC, p.penaltySeconds ASC")
    List<TournamentParticipant> findLeaderboard(@Param("tournamentId") UUID tournamentId);
}


