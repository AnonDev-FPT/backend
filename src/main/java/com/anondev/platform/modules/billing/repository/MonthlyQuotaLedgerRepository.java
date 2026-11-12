package com.anondev.platform.modules.billing.repository;

import com.anondev.platform.modules.billing.entity.MonthlyQuotaLedger;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyQuotaLedgerRepository extends JpaRepository<MonthlyQuotaLedger, UUID> {
    Optional<MonthlyQuotaLedger> findByCompanyIdAndCycleYearAndCycleMonth(UUID companyId, Integer cycleYear, Integer cycleMonth);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MonthlyQuotaLedger m WHERE m.companyId = :companyId AND m.cycleYear = :cycleYear AND m.cycleMonth = :cycleMonth")
    Optional<MonthlyQuotaLedger> findByCompanyIdAndCycleYearAndCycleMonthForUpdate(
            @Param("companyId") UUID companyId,
            @Param("cycleYear") Integer cycleYear,
            @Param("cycleMonth") Integer cycleMonth
    );
}
