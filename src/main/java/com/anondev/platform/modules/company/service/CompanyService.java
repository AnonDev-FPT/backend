package com.anondev.platform.modules.company.service;

import com.anondev.platform.modules.company.dto.CompanyProfileResponse;
import com.anondev.platform.modules.company.dto.KybVerificationRequest;
import com.anondev.platform.modules.company.entity.Company;

import java.util.UUID;

public interface CompanyService {
    Company getCompanyByUserId(UUID userId);
    CompanyProfileResponse getCompanyProfileByUserEmail(String userEmail);
    Company updateKyb(UUID userId, KybVerificationRequest request);
    CompanyProfileResponse updateKybByUserEmail(String userEmail, KybVerificationRequest request);
    CompanyProfileResponse verifyCompanyTaxId(UUID companyId, boolean isVerified);
}
