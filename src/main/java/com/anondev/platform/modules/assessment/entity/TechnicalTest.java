package com.anondev.platform.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "technical_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_posting_id", nullable = false, unique = true)
    private UUID jobPostingId;

    @Column(name = "creation_method", nullable = false, length = 20)
    private String creationMethod; // MANUAL, AI_FROM_JD

    @Column(nullable = false)
    private String title;

    @Column(name = "problem_statement", nullable = false, columnDefinition = "TEXT")
    private String problemStatement;

    @Column(name = "time_limit_minutes", nullable = false)
    @Builder.Default
    private Integer timeLimitMinutes = 45;

    @Column(name = "starter_code_json", columnDefinition = "JSONB")
    @Builder.Default
    private String starterCodeJson = "{}";

    @Column(name = "test_cases_json", nullable = false, columnDefinition = "JSONB")
    @Builder.Default
    private String testCasesJson = "[]";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
