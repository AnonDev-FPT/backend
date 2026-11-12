package com.anondev.platform.modules.tournament.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.tournament.dto.AddProblemRequest;
import com.anondev.platform.modules.tournament.dto.CreateTournamentRequest;
import com.anondev.platform.modules.tournament.entity.CodingTournament;
import com.anondev.platform.modules.tournament.entity.TournamentProblem;
import com.anondev.platform.modules.tournament.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tournaments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Tournament", description = "Admin Coding Arena Management APIs")
public class AdminTournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    @Operation(summary = "Create a new coding tournament")
    public ResponseEntity<ApiResponse<CodingTournament>> createTournament(
            @Valid @RequestBody CreateTournamentRequest request
    ) {
        CodingTournament tournament = tournamentService.createTournament(request);
        return ResponseEntity.ok(ApiResponse.ok("Tournament created successfully", tournament));
    }

    @PostMapping("/{id}/problems")
    @Operation(summary = "Add a programming problem to tournament")
    public ResponseEntity<ApiResponse<TournamentProblem>> addProblem(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AddProblemRequest request
    ) {
        TournamentProblem problem = tournamentService.addProblem(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Problem added successfully to tournament", problem));
    }
}

