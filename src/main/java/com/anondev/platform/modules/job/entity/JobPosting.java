package com.anondev.platform.modules.job.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String title;

    @Column(name = "description_sanitized", nullable = false, columnDefinition = "TEXT")
    private String descriptionSanitized;

    @Column(name = "province_city", nullable = false, length = 100)
    private String provinceCity;

    @Column(name = "work_mode", nullable = false, length = 20)
    private String workMode; // REMOTE, OFFLINE, HYBRID

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "min_pass_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal minPassScore = BigDecimal.valueOf(50.00);

    @Column(name = "funding_source", nullable = false, length = 20)
    private String fundingSource; // FREE_TRIAL, SINGLE_PURCHASE, SUBSCRIPTION_SLOT

    @Column(name = "is_priority_display")
    @Builder.Default
    private Boolean isPriorityDisplay = false;

    @CreationTimestamp
    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "expires_at", nullable = false)
    @Builder.Default
    private Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // DRAFT, ACTIVE, CLOSED, EXPIRED
}
