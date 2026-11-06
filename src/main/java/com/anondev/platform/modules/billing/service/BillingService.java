package com.anondev.platform.modules.billing.service;

import com.anondev.platform.modules.billing.dto.*;
import com.anondev.platform.modules.billing.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {
    public BigDecimal mockTopup(UUID companyId, MockTopupRequest request) {
        return request.getAmount();
    }

    public CompanySubscription subscribe(UUID companyId, SubscribeRequest request) {
        return null;
    }

    public List<SubscriptionPlan> getAllPlans() {
        return List.of();
    }
}
