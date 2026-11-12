package com.anondev.platform.modules.tournament.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List all coding tournaments (UPCOMING, ONGOING, FINISHED)")
    public ResponseEntity<ApiResponse<List<TournamentSummaryDto>>> getTournaments(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID candidateId = getCandidateId(userDetails);
        List<TournamentSummaryDto> list = tournamentService.getAllTournaments(candidateId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information about a tournament")
    public ResponseEntity<ApiResponse<TournamentSummaryDto>> getTournamentDetails(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID candidateId = getCandidateId(userDetails);
        TournamentSummaryDto dto = tournamentService.getTournamentDetails(id, candidateId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Register candidate to participate in tournament")
    public ResponseEntity<ApiResponse<TournamentParticipant>> joinTournament(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        TournamentParticipant participant = tournamentService.joinTournament(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Successfully registered for tournament", participant));
    }

    @GetMapping("/{id}/problems")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get list of problems for an ongoing tournament (registered candidates only)")
    public ResponseEntity<ApiResponse<List<TournamentProblem>>> getProblems(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<TournamentProblem> problems = tournamentService.getProblems(id, user.getId());
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
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        TournamentSubmission submission = tournamentService.submitSolution(id, problemId, user.getId(), request);
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

    private UUID getCandidateId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return null;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(null);
    }
}

