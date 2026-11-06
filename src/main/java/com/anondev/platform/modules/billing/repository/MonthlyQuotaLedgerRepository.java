package com.anondev.platform.modules.billing.repository;

import com.anondev.platform.modules.billing.entity.MonthlyQuotaLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyQuotaLedgerRepository extends JpaRepository<MonthlyQuotaLedger, UUID> {
    Optional<MonthlyQuotaLedger> findByCompanyIdAndCycleYearAndCycleMonth(UUID companyId, Integer cycleYear, Integer cycleMonth);
}
