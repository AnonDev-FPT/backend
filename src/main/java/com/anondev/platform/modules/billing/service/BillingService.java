package com.anondev.platform.modules.billing.service;

import com.anondev.platform.modules.billing.dto.MockTopupRequest;
import com.anondev.platform.modules.billing.dto.SubscribeRequest;
import com.anondev.platform.modules.billing.entity.CompanySubscription;
import com.anondev.platform.modules.billing.entity.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BillingService {

    BigDecimal mockTopupByUserEmail(String userEmail, MockTopupRequest request);

    CompanySubscription subscribeByUserEmail(String userEmail, SubscribeRequest request);

    BigDecimal mockTopup(UUID companyId, MockTopupRequest request);

    CompanySubscription subscribe(UUID companyId, SubscribeRequest request);

    List<SubscriptionPlan> getAllPlans();
}
