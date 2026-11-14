package com.anondev.platform.modules.billing.service;

import com.anondev.platform.modules.billing.dto.RefundDisputeRequest;
import com.anondev.platform.modules.billing.entity.RefundDispute;

import java.util.UUID;

public interface DisputeRefundService {

    RefundDispute createDisputeByUserEmail(String userEmail, RefundDisputeRequest request);

    void resolveDisputeByUserEmail(String adminEmail, UUID disputeId, boolean approve);

    RefundDispute createDispute(UUID companyId, RefundDisputeRequest request);

    void resolveDispute(UUID adminId, UUID disputeId, boolean approve);
}
