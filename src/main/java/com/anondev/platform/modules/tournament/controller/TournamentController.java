package com.anondev.platform.modules.tournament.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.tournament.dto.SubmitTournamentSolutionRequest;
import com.anondev.platform.modules.tournament.dto.TournamentLeaderboardDto;
import com.anondev.platform.modules.tournament.dto.TournamentSummaryDto;
import com.anondev.platform.modules.tournament.entity.TournamentParticipant;
import com.anondev.platform.modules.tournament.entity.TournamentProblem;
import com.anondev.platform.modules.tournament.entity.TournamentSubmission;
import com.anondev.platform.modules.tournament.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Tag(name = "Tournament", description = "Coding Arena & Competitive Tournaments (B2C)")
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    @Operation(summary = "List all coding tournaments (UPCOMING, ONGOING, FINISHED)")
    public ResponseEntity<ApiResponse<List<TournamentSummaryDto>>> getTournaments(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<TournamentSummaryDto> list = tournamentService.getAllTournamentsByUserEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information about a tournament")
    public ResponseEntity<ApiResponse<TournamentSummaryDto>> getTournamentDetails(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        TournamentSummaryDto dto = tournamentService.getTournamentDetailsByUserEmail(id, email);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Register candidate to participate in tournament")
    public ResponseEntity<ApiResponse<TournamentParticipant>> joinTournament(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TournamentParticipant participant = tournamentService.joinTournamentByUserEmail(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Successfully registered for tournament", participant));
    }

    @GetMapping("/{id}/problems")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get list of problems for an ongoing tournament (registered candidates only)")
    public ResponseEntity<ApiResponse<List<TournamentProblem>>> getProblems(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<TournamentProblem> problems = tournamentService.getProblemsByUserEmail(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(problems));
    }

    @PostMapping("/{id}/problems/{problemId}/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit source code for a problem in an ongoing tournament")
    public ResponseEntity<ApiResponse<TournamentSubmission>> submitSolution(
            @PathVariable("id") UUID id,
            @PathVariable("problemId") UUID problemId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubmitTournamentSolutionRequest request
    ) {
        TournamentSubmission submission = tournamentService.submitSolutionByUserEmail(id, problemId, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Solution evaluated: " + submission.getVerdict(), submission));
    }

    @GetMapping("/{id}/leaderboard")
    @Operation(summary = "Get live leaderboard of a tournament (anonymized candidate codes)")
    public ResponseEntity<ApiResponse<List<TournamentLeaderboardDto>>> getLeaderboard(
            @PathVariable("id") UUID id
    ) {
        List<TournamentLeaderboardDto> leaderboard = tournamentService.getLeaderboard(id);
        return ResponseEntity.ok(ApiResponse.ok(leaderboard));
    }
}
