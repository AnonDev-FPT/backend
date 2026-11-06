package com.anondev.platform.modules.tournament.repository;

import com.anondev.platform.modules.tournament.entity.CodingTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CodingTournamentRepository extends JpaRepository<CodingTournament, UUID> {
}
