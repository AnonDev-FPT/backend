package com.anondev.platform.modules.candidate.repository;

import com.anondev.platform.modules.candidate.entity.CandidateBlindProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlindProfileRepository extends JpaRepository<CandidateBlindProfile, UUID> {
    Optional<CandidateBlindProfile> findByCandidateCode(String candidateCode);
}
