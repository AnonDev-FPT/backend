package com.anondev.platform.modules.company.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false, unique = true)
    private UUID ownerUserId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 50)
    private String taxId;

    @Column(name = "business_license_url", columnDefinition = "TEXT")
    private String businessLicenseUrl;

    @Column(name = "is_tax_verified")
    @Builder.Default
    private Boolean isTaxVerified = false;

    @Column(name = "has_used_free_job")
    @Builder.Default
    private Boolean hasUsedFreeJob = false;

    @Column(name = "credit_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal creditBalance = BigDecimal.ZERO;

    @Column(name = "held_credit_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal heldCreditBalance = BigDecimal.ZERO;

    @Column(name = "status_update_counter")
    @Builder.Default
    private Integer statusUpdateCounter = 0;

    @Column(name = "is_blocked_by_overdue_status")
    @Builder.Default
    private Boolean isBlockedByOverdueStatus = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
