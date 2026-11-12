package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.TournamentProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentProblemRepository extends JpaRepository<TournamentProblem, UUID> {
    List<TournamentProblem> findByTournamentIdOrderByCreatedAtAsc(UUID tournamentId);
    long countByTournamentId(UUID tournamentId);
}

