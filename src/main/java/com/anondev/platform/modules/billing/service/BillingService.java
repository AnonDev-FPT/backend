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

        BigDecimal price = "ANNUAL".equalsIgnoreCase(request.getBillingCycle())
                ? (plan.getAnnualPrice() != null ? plan.getAnnualPrice() : plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12)))
                : plan.getMonthlyPrice();

        // Deduct subscription fee from wallet balance if plan is not free
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentCredit = company.getCreditBalance() != null ? company.getCreditBalance() : BigDecimal.ZERO;
            BigDecimal currentHeld = company.getHeldCreditBalance() != null ? company.getHeldCreditBalance() : BigDecimal.ZERO;
            BigDecimal availableCredit = currentCredit.subtract(currentHeld);

            if (availableCredit.compareTo(price) < 0) {
                throw new BusinessException("Insufficient available wallet balance to subscribe to plan '" + plan.getPlanName() + "' (" + price + " VND required). Current available: " + availableCredit + " VND");
            }

            company.setCreditBalance(currentCredit.subtract(price));
            companyRepository.save(company);

            walletTransactionRepository.save(WalletTransaction.builder()
                    .companyId(companyId)
                    .transactionType("SUBSCRIPTION_PURCHASE")
                    .amount(price)
                    .paymentGateway("INTERNAL_WALLET")
                    .description("Subscription fee for plan: " + plan.getPlanName() + " (" + request.getBillingCycle().toUpperCase() + ")")
                    .build());
        }

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
