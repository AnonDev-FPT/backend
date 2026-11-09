package com.anondev.platform.modules.billing.service;

import com.anondev.platform.modules.billing.dto.RefundDisputeRequest;
import com.anondev.platform.modules.billing.entity.RefundDispute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeRefundService {
    public RefundDispute createDispute(UUID companyId, RefundDisputeRequest request) {
        return null;
    }

    public void resolveDispute(UUID adminId, UUID disputeId, boolean approve) {
    }
}
