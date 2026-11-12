package com.anondev.platform.modules.tournament.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournament_problems")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    @Column(name = "problem_statement", nullable = false, columnDefinition = "TEXT")
    private String problemStatement;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 100;

    @Column(name = "time_limit_ms", nullable = false)
    @Builder.Default
    private Integer timeLimitMs = 1000;

    @Column(name = "memory_limit_kb", nullable = false)
    @Builder.Default
    private Integer memoryLimitKb = 256000;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "starter_code_json", columnDefinition = "JSONB")
    @Builder.Default
    private String starterCodeJson = "{}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "test_cases_json", nullable = false, columnDefinition = "JSONB")
    @Builder.Default
    private String testCasesJson = "[]";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

