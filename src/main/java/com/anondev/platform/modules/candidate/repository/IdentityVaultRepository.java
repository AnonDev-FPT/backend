package com.anondev.platform.modules.candidate.repository;

import com.anondev.platform.modules.candidate.entity.CandidateIdentityVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentityVaultRepository extends JpaRepository<CandidateIdentityVault, UUID> {
}
