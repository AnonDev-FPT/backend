package com.anondev.platform.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role; // CANDIDATE, EMPLOYER, ADMIN

    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = true;

    @Column(name = "is_phone_verified")
    @Builder.Default
    private Boolean isPhoneVerified = true;

    @Column(name = "account_status", length = 20)
    @Builder.Default
    private String accountStatus = "ACTIVE"; // ACTIVE, WARNED, RESTRICTED, BANNED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
