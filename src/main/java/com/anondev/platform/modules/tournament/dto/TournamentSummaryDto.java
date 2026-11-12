package com.anondev.platform.modules.tournament.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSummaryDto {
    private UUID id;
    private String title;
    private BigDecimal entryFee;
    private BigDecimal prizePoolAmount;
    private Instant startTime;
    private Instant endTime;
    private String status;
    private long participantCount;
    private long problemCount;
    private Boolean isRegistered;
}

