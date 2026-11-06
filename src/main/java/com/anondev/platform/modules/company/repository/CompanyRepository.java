package com.anondev.platform.modules.company.repository;

import com.anondev.platform.modules.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByOwnerUserId(UUID ownerUserId);
    Optional<Company> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);
}
