package com.anondev.platform.modules.billing.repository;

import com.anondev.platform.modules.billing.entity.RefundDispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundDisputeRepository extends JpaRepository<RefundDispute, UUID> {
    Optional<RefundDispute> findByInvitationId(UUID invitationId);
    List<RefundDispute> findByCompanyId(UUID companyId);
    List<RefundDispute> findByDisputeStatus(String status);
}
