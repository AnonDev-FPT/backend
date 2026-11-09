package com.anondev.platform.modules.candidate.entity;

import com.anondev.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candidate_identity_vaults")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateIdentityVault implements Persistable<UUID> {

    @Id
    @Column(name = "candidate_id")
    private UUID candidateId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return candidateId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PrePersist
    void markNotNew() {
        this.isNew = false;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "real_full_name", nullable = false)
    private String realFullName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "university_name")
    private String universityName;

    @Column(length = 20)
    private String gender;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "original_cv_url", nullable = false, columnDefinition = "TEXT")
    private String originalCvUrl;

    @Column(name = "sanitized_cv_url", columnDefinition = "TEXT")
    private String sanitizedCvUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "portfolio_links", columnDefinition = "JSONB")
    @Builder.Default
    private String portfolioLinks = "[]";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
