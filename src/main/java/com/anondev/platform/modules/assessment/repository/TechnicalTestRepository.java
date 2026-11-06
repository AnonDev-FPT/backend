package com.anondev.platform.modules.assessment.repository;

import com.anondev.platform.modules.assessment.entity.TechnicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicalTestRepository extends JpaRepository<TechnicalTest, UUID> {
    Optional<TechnicalTest> findByJobPostingId(UUID jobPostingId);
}
