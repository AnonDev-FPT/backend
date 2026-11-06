package com.anondev.platform.modules.assessment.repository;

import com.anondev.platform.modules.assessment.entity.ProctoringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProctoringLogRepository extends JpaRepository<ProctoringLog, UUID> {
    Optional<ProctoringLog> findByAttemptId(UUID attemptId);
}
