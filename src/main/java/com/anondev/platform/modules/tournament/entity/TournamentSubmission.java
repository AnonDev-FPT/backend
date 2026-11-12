package com.anondev.platform.modules.tournament.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournament_submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "problem_id", nullable = false)
    private UUID problemId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String language = "JAVA";

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String verdict = "PENDING"; // ACCEPTED, WRONG_ANSWER, COMPILE_ERROR, RUNTIME_ERROR, TIME_LIMIT_EXCEEDED

    @Column(name = "passed_test_cases", nullable = false)
    @Builder.Default
    private Integer passedTestCases = 0;

    @Column(name = "total_test_cases", nullable = false)
    @Builder.Default
    private Integer totalTestCases = 0;

    @Column(name = "execution_time_ms")
    @Builder.Default
    private Integer executionTimeMs = 0;

    @Column(name = "score_awarded", nullable = false)
    @Builder.Default
    private Integer scoreAwarded = 0;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private Instant submittedAt;
}

