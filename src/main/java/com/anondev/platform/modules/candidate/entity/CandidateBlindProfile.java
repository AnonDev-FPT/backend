package com.anondev.platform.modules.candidate.entity;

import com.anondev.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candidate_blind_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateBlindProfile {

    @Id
    @Column(name = "candidate_id")
    private UUID candidateId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "candidate_id")
    private User user;

    @Column(name = "candidate_code", nullable = false, unique = true, length = 20)
    private String candidateCode;

    @Column(name = "province_city", nullable = false, length = 100)
    private String provinceCity;

    @Column(name = "work_mode", nullable = false, length = 20)
    private String workMode; // REMOTE, OFFLINE, HYBRID

    @Column(name = "experience_level", length = 30)
    private String experienceLevel; // INTERN, FRESHER, JUNIOR, MID, SENIOR

    @Column(name = "expected_salary_min", precision = 12, scale = 2)
    private BigDecimal expectedSalaryMin;

    @Column(name = "expected_salary_max", precision = 12, scale = 2)
    private BigDecimal expectedSalaryMax;

    @Column(name = "primary_tech_stack", columnDefinition = "JSONB")
    @Builder.Default
    private String primaryTechStack = "[]";

    @Column(name = "skill_radar_scores", columnDefinition = "JSONB")
    @Builder.Default
    private String skillRadarScores = "{}";

    @Column(name = "sanitized_bio", columnDefinition = "TEXT")
    private String sanitizedBio;

    @Column(name = "overall_ranking_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overallRankingScore = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
