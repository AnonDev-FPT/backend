package com.anondev.platform.modules.tournament.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournament_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "candidate_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false)
    private Instant registeredAt;

    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Integer totalScore = 0;

    @Column(name = "penalty_seconds", nullable = false)
    @Builder.Default
    private Integer penaltySeconds = 0;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "is_disqualified")
    @Builder.Default
    private Boolean isDisqualified = false;
}

