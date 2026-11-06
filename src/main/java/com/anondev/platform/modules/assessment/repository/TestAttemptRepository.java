package com.anondev.platform.modules.assessment.repository;

import com.anondev.platform.modules.assessment.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {
    Optional<TestAttempt> findByJobPostingIdAndCandidateIdAndIsRetakeAttempt(UUID jobPostingId, UUID candidateId, Boolean isRetakeAttempt);
    List<TestAttempt> findByJobPostingIdAndIsPassedThreshold(UUID jobPostingId, Boolean isPassedThreshold);
    boolean existsByJobPostingIdAndCandidateId(UUID jobPostingId, UUID candidateId);
}
