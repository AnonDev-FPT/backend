package com.anondev.platform.modules.tournament.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coding_tournaments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTournament {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "entry_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal entryFee = BigDecimal.valueOf(50000);

    @Column(name = "prize_pool_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal prizePoolAmount = BigDecimal.valueOf(5000000);

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(length = 20)
    @Builder.Default
    private String status = "UPCOMING";
}
