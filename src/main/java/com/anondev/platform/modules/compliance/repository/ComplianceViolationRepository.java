package com.anondev.platform.modules.compliance.repository;

import com.anondev.platform.modules.compliance.entity.ComplianceViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplianceViolationRepository extends JpaRepository<ComplianceViolation, UUID> {
}
