package com.anondev.platform.modules.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plan_code", nullable = false, unique = true, length = 30)
    private String planCode;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "monthly_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", precision = 12, scale = 2)
    private BigDecimal annualPrice;

    @Column(name = "max_concurrent_jobs", nullable = false)
    private Integer maxConcurrentJobs;

    @Column(name = "monthly_unlock_quota", nullable = false)
    private Integer monthlyUnlockQuota;

    @Column(name = "extra_unlock_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraUnlockPrice;

    @Column(name = "has_priority_display")
    private Boolean hasPriorityDisplay;

    @Column(name = "report_tier", length = 20)
    private String reportTier;

    @Column(name = "support_tier", length = 20)
    private String supportTier;
}
