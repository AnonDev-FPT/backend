package com.anondev.platform.modules.billing.repository;

import com.anondev.platform.modules.billing.entity.CompanySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, UUID> {
    Optional<CompanySubscription> findByCompanyIdAndStatus(UUID companyId, String status);
}
