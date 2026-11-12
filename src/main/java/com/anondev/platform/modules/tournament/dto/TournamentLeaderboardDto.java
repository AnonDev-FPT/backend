package com.anondev.platform.modules.tournament.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentLeaderboardDto {
    private Integer rank;
    private String candidateCode;
    private UUID candidateId;
    private Integer totalScore;
    private Integer penaltySeconds;
    private long problemsSolved;
}

