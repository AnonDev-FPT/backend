package com.anondev.platform.modules.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monthly_quota_ledgers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "cycle_year", "cycle_month"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyQuotaLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "cycle_year", nullable = false)
    private Integer cycleYear;

    @Column(name = "cycle_month", nullable = false)
    private Integer cycleMonth;

    @Column(name = "allocated_quota", nullable = false)
    @Builder.Default
    private Integer allocatedQuota = 0;

    @Column(name = "rewarded_quota", nullable = false)
    @Builder.Default
    private Integer rewardedQuota = 0;

    @Column(name = "refunded_quota", nullable = false)
    @Builder.Default
    private Integer refundedQuota = 0;

    @Column(name = "held_quota", nullable = false)
    @Builder.Default
    private Integer heldQuota = 0;

    @Column(name = "used_quota", nullable = false)
    @Builder.Default
    private Integer usedQuota = 0;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
