package com.anondev.platform.modules.company.repository;

import com.anondev.platform.modules.company.entity.Company;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByOwnerUserId(UUID ownerUserId);
    Optional<Company> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Company c WHERE c.id = :id")
    Optional<Company> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Company c WHERE c.ownerUserId = :ownerUserId")
    Optional<Company> findByOwnerUserIdForUpdate(@Param("ownerUserId") UUID ownerUserId);
}
