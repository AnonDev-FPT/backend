package com.anondev.platform.modules.billing.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.billing.dto.*;
import com.anondev.platform.modules.billing.entity.*;
import com.anondev.platform.modules.billing.repository.*;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final CompanyRepository companyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanySubscriptionRepository subscriptionRepository;
    private final MonthlyQuotaLedgerRepository quotaLedgerRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public BigDecimal mockTopup(UUID companyId, MockTopupRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Company not found"));

        BigDecimal newBalance = (company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO)
                .add(request.getAmount());
        company.setCreditBalance(newBalance);
        companyRepository.save(company);

        walletTransactionRepository.save(WalletTransaction.builder()
                .companyId(companyId)
                .transactionType("TOPUP")
                .amount(request.getAmount())
                .paymentGateway("INTERNAL_WALLET")
                .description("Mock Topup via Internal Gateway")
                .build());

        return newBalance;
    }

    @Transactional
    public CompanySubscription subscribe(UUID companyId, SubscribeRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Company not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCode(request.getPlanCode())
                .orElseThrow(() -> new BusinessException("Subscription plan not found: " + request.getPlanCode()));

        Instant now = Instant.now();
        Instant end = "ANNUAL".equalsIgnoreCase(request.getBillingCycle())
                ? now.plus(365, ChronoUnit.DAYS)
                : now.plus(30, ChronoUnit.DAYS);

        CompanySubscription sub = CompanySubscription.builder()
                .companyId(companyId)
                .planId(plan.getId())
                .billingCycle(request.getBillingCycle().toUpperCase())
                .lockedPrice(plan.getMonthlyPrice())
                .startDate(now)
                .endDate(end)
                .status("ACTIVE")
                .build();
        CompanySubscription savedSub = subscriptionRepository.save(sub);
        final UUID subId = savedSub.getId();

        // Allocate monthly quota
        java.time.LocalDate today = java.time.LocalDate.now();
        MonthlyQuotaLedger ledger = quotaLedgerRepository.findByCompanyIdAndCycleYearAndCycleMonth(
                companyId, today.getYear(), today.getMonthValue()
        ).orElseGet(() -> MonthlyQuotaLedger.builder()
                .companyId(companyId)
                .subscriptionId(subId)
                .cycleYear(today.getYear())
                .cycleMonth(today.getMonthValue())
                .expiresAt(end)
                .build()
        );

        ledger.setSubscriptionId(subId);
        ledger.setAllocatedQuota(plan.getMonthlyUnlockQuota());
        quotaLedgerRepository.save(ledger);

        return savedSub;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }
}
