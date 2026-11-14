package com.anondev.platform.modules.company.service.impl;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.company.dto.CompanyProfileResponse;
import com.anondev.platform.modules.company.dto.KybVerificationRequest;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Company getCompanyByUserId(UUID userId) {
        return companyRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BusinessException("Company profile not found for current user."));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyProfileResponse getCompanyProfileByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        return CompanyProfileResponse.fromEntity(getCompanyByUserId(user.getId()));
    }

    @Override
    @Transactional
    public Company updateKyb(UUID userId, KybVerificationRequest request) {
        Company company = getCompanyByUserId(userId);
        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
        if (request.getTaxId() != null && !request.getTaxId().equals(company.getTaxId())) {
            if (companyRepository.existsByTaxId(request.getTaxId())) {
                throw new BusinessException("Tax ID already registered by another company.");
            }
            company.setTaxId(request.getTaxId());
            company.setIsTaxVerified(false); // Reset verification on tax ID change
        }
        if (request.getBusinessLicenseUrl() != null) {
            company.setBusinessLicenseUrl(request.getBusinessLicenseUrl());
        }
        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public CompanyProfileResponse updateKybByUserEmail(String userEmail, KybVerificationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        return CompanyProfileResponse.fromEntity(updateKyb(user.getId(), request));
    }

    @Override
    @Transactional
    public CompanyProfileResponse verifyCompanyTaxId(UUID companyId, boolean isVerified) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Company not found."));
        company.setIsTaxVerified(isVerified);
        return CompanyProfileResponse.fromEntity(companyRepository.save(company));
    }
}
