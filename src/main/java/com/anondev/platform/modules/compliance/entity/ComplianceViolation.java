package com.anondev.platform.modules.compliance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compliance_violations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceViolation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "violator_user_id", nullable = false)
    private UUID violatorUserId;

    @Column(name = "reported_by_user_id")
    private UUID reportedByUserId;

    @Column(name = "rule_code", nullable = false, length = 15)
    private String ruleCode;

    @Column(name = "violation_type", nullable = false, length = 50)
    private String violationType;

    @Column(name = "penalty_applied", nullable = false, length = 40)
    private String penaltyApplied;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
