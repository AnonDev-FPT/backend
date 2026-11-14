package com.anondev.platform.modules.tournament.service;

import com.anondev.platform.modules.tournament.dto.AddProblemRequest;
import com.anondev.platform.modules.tournament.dto.CreateTournamentRequest;
import com.anondev.platform.modules.tournament.dto.SubmitTournamentSolutionRequest;
import com.anondev.platform.modules.tournament.dto.TournamentLeaderboardDto;
import com.anondev.platform.modules.tournament.dto.TournamentSummaryDto;
import com.anondev.platform.modules.tournament.entity.CodingTournament;
import com.anondev.platform.modules.tournament.entity.TournamentParticipant;
import com.anondev.platform.modules.tournament.entity.TournamentProblem;
import com.anondev.platform.modules.tournament.entity.TournamentSubmission;

import java.util.List;
import java.util.UUID;

public interface TournamentService {

    List<TournamentSummaryDto> getAllTournamentsByUserEmail(String userEmail);

    TournamentSummaryDto getTournamentDetailsByUserEmail(UUID id, String userEmail);

    TournamentParticipant joinTournamentByUserEmail(UUID id, String userEmail);

    List<TournamentProblem> getProblemsByUserEmail(UUID id, String userEmail);

    TournamentSubmission submitSolutionByUserEmail(UUID id, UUID problemId, String userEmail, SubmitTournamentSolutionRequest request);

    List<TournamentSummaryDto> getAllTournaments(UUID candidateId);

    TournamentSummaryDto getTournamentDetails(UUID tournamentId, UUID candidateId);

    CodingTournament createTournament(CreateTournamentRequest request);

    TournamentProblem addProblem(UUID tournamentId, AddProblemRequest request);

    TournamentParticipant joinTournament(UUID tournamentId, UUID candidateId);

    List<TournamentProblem> getProblems(UUID tournamentId, UUID candidateId);

    TournamentSubmission submitSolution(UUID tournamentId, UUID problemId, UUID candidateId, SubmitTournamentSolutionRequest request);

    List<TournamentLeaderboardDto> getLeaderboard(UUID tournamentId);
}
