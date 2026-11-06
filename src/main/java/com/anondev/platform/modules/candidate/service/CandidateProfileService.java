package com.anondev.platform.modules.candidate.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.candidate.dto.BlindProfileResponseDto;
import com.anondev.platform.modules.candidate.dto.IdentityVaultDto;
import com.anondev.platform.modules.candidate.dto.UpdateProfileRequest;
import com.anondev.platform.modules.candidate.entity.CandidateBlindProfile;
import com.anondev.platform.modules.candidate.entity.CandidateIdentityVault;
import com.anondev.platform.modules.candidate.repository.BlindProfileRepository;
import com.anondev.platform.modules.candidate.repository.IdentityVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final BlindProfileRepository blindProfileRepository;
    private final IdentityVaultRepository identityVaultRepository;
    private final AiSanitizerService aiSanitizerService;

    @Transactional(readOnly = true)
    public Map<String, Object> getCandidateVaultData(UUID candidateId) {
        CandidateIdentityVault vault = identityVaultRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Candidate vault not found"));

        CandidateBlindProfile blind = blindProfileRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Blind profile not found"));

        IdentityVaultDto vaultDto = IdentityVaultDto.builder()
                .realFullName(vault.getRealFullName())
                .contactEmail(vault.getContactEmail())
                .contactPhone(vault.getContactPhone())
                .universityName(vault.getUniversityName())
                .gender(vault.getGender())
                .avatarUrl(vault.getAvatarUrl())
                .originalCvUrl(vault.getOriginalCvUrl())
                .sanitizedCvUrl(vault.getSanitizedCvUrl())
                .build();

        BlindProfileResponseDto blindDto = BlindProfileResponseDto.builder()
                .candidateCode(blind.getCandidateCode())
                .provinceCity(blind.getProvinceCity())
                .workMode(blind.getWorkMode())
                .experienceLevel(blind.getExperienceLevel())
                .expectedSalaryMin(blind.getExpectedSalaryMin())
                .expectedSalaryMax(blind.getExpectedSalaryMax())
                .sanitizedBio(blind.getSanitizedBio())
                .overallRankingScore(blind.getOverallRankingScore())
                .build();

        return Map.of(
                "vault", vaultDto,
                "blindProfile", blindDto
        );
    }

    @Transactional
    public void updateCandidateVault(UUID candidateId, UpdateProfileRequest request) {
        CandidateIdentityVault vault = identityVaultRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Candidate vault not found"));

        CandidateBlindProfile blind = blindProfileRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Blind profile not found"));

        if (request.getRealFullName() != null) vault.setRealFullName(request.getRealFullName());
        if (request.getContactEmail() != null) vault.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) vault.setContactPhone(request.getContactPhone());
        if (request.getUniversityName() != null) vault.setUniversityName(request.getUniversityName());
        if (request.getGender() != null) vault.setGender(request.getGender());
        if (request.getAvatarUrl() != null) vault.setAvatarUrl(request.getAvatarUrl());
        if (request.getOriginalCvUrl() != null) vault.setOriginalCvUrl(request.getOriginalCvUrl());

        if (request.getProvinceCity() != null) blind.setProvinceCity(request.getProvinceCity());
        if (request.getWorkMode() != null) blind.setWorkMode(request.getWorkMode());
        if (request.getExperienceLevel() != null) blind.setExperienceLevel(request.getExperienceLevel());
        if (request.getExpectedSalaryMin() != null) blind.setExpectedSalaryMin(request.getExpectedSalaryMin());
        if (request.getExpectedSalaryMax() != null) blind.setExpectedSalaryMax(request.getExpectedSalaryMax());

        // Sanitization using Strategy Pattern (BR-03)
        if (request.getBio() != null) {
            String sanitized = aiSanitizerService.sanitizeText(request.getBio());
            blind.setSanitizedBio(sanitized);
        }

        identityVaultRepository.save(vault);
        blindProfileRepository.save(blind);
    }
}
