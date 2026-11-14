package com.anondev.platform.modules.billing.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final CompanyRepository companyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanySubscriptionRepository subscriptionRepository;
    private final MonthlyQuotaLedgerRepository quotaLedgerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BigDecimal mockTopupByUserEmail(String userEmail, MockTopupRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        return mockTopup(company.getId(), request);
    }

    @Transactional
    public CompanySubscription subscribeByUserEmail(String userEmail, SubscribeRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        Company company = companyRepository.findByOwnerUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Company profile not found for user"));
        return subscribe(company.getId(), request);
    }

    @Transactional
    public BigDecimal mockTopup(UUID companyId, MockTopupRequest request) {
        Company company = companyRepository.findByIdForUpdate(companyId)
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
        Company company = companyRepository.findByIdForUpdate(companyId)
                .orElseThrow(() -> new BusinessException("Company not found"));

        // Check if company already has an active subscription
        Optional<CompanySubscription> existingSub = subscriptionRepository.findByCompanyIdAndStatus(companyId, "ACTIVE");
        if (existingSub.isPresent()) {
            CompanySubscription active = existingSub.get();
            if (active.getEndDate() == null || active.getEndDate().isAfter(Instant.now())) {
                throw new BusinessException("Company already has an active subscription until " + active.getEndDate() + ". Please wait until it expires or contact support to upgrade (BR-26).");
            } else {
                active.setStatus("EXPIRED");
                subscriptionRepository.save(active);
            }
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCode(request.getPlanCode())
                .orElseThrow(() -> new BusinessException("Subscription plan not found: " + request.getPlanCode()));

        BigDecimal price = "ANNUAL".equalsIgnoreCase(request.getBillingCycle())
                ? (plan.getAnnualPrice() != null ? plan.getAnnualPrice() : (plan.getMonthlyPrice() != null ? plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12)) : BigDecimal.ZERO))
                : (plan.getMonthlyPrice() != null ? plan.getMonthlyPrice() : BigDecimal.ZERO);

        // Deduct subscription fee from wallet balance if plan is not free
        if (price.compareTo(BigDecimal.ZERO) > 0) {
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

        BigDecimal lockedPrice = "ANNUAL".equalsIgnoreCase(request.getBillingCycle())
                ? (plan.getAnnualPrice() != null ? plan.getAnnualPrice() : price)
                : (plan.getMonthlyPrice() != null ? plan.getMonthlyPrice() : price);

        CompanySubscription sub = CompanySubscription.builder()
                .companyId(companyId)
                .planId(plan.getId())
                .billingCycle(request.getBillingCycle().toUpperCase())
                .lockedPrice(lockedPrice)
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
