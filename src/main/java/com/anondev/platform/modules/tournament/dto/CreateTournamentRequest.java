package com.anondev.platform.modules.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentRequest {

    @NotBlank(message = "Tournament title is required")
    @Size(max = 255, message = "Tournament title cannot exceed 255 characters")
    private String title;

    @PositiveOrZero(message = "Entry fee must be greater than or equal to 0")
    @Builder.Default
    private BigDecimal entryFee = BigDecimal.valueOf(50000);

    @PositiveOrZero(message = "Prize pool must be greater than or equal to 0")
    @Builder.Default
    private BigDecimal prizePoolAmount = BigDecimal.valueOf(5000000);

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;
}

