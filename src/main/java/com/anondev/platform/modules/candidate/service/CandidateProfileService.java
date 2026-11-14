package com.anondev.platform.modules.candidate.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.integration.ai.AiSanitizerService;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.candidate.dto.BlindProfileResponseDto;
import com.anondev.platform.modules.candidate.dto.IdentityVaultDto;
import com.anondev.platform.modules.candidate.dto.UpdateProfileRequest;
import com.anondev.platform.modules.candidate.entity.CandidateBlindProfile;
import com.anondev.platform.modules.candidate.entity.CandidateIdentityVault;
import com.anondev.platform.modules.candidate.repository.BlindProfileRepository;
import com.anondev.platform.modules.candidate.repository.IdentityVaultRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final BlindProfileRepository blindProfileRepository;
    private final IdentityVaultRepository identityVaultRepository;
    private final AiSanitizerService aiSanitizerService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Map<String, Object> getCandidateVaultDataByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        return getCandidateVaultData(user.getId());
    }

    @Transactional
    public void updateCandidateVaultByUserEmail(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        updateCandidateVault(user.getId(), request);
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

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
                .portfolioLinks(parseJsonList(vault.getPortfolioLinks()))
                .build();

        BlindProfileResponseDto blindDto = BlindProfileResponseDto.builder()
                .candidateCode(blind.getCandidateCode())
                .provinceCity(blind.getProvinceCity())
                .workMode(blind.getWorkMode())
                .experienceLevel(blind.getExperienceLevel())
                .expectedSalaryMin(blind.getExpectedSalaryMin())
                .expectedSalaryMax(blind.getExpectedSalaryMax())
                .primaryTechStack(parseJsonList(blind.getPrimaryTechStack()))
                .skillRadarScores(parseJsonMap(blind.getSkillRadarScores()))
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
        if (request.getExpectedSalaryMin() != null && request.getExpectedSalaryMax() != null
                && request.getExpectedSalaryMin().compareTo(request.getExpectedSalaryMax()) > 0) {
            throw new BusinessException("Expected minimum salary cannot exceed maximum salary.");
        }

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
        if (request.getPortfolioLinks() != null) {
            try {
                vault.setPortfolioLinks(objectMapper.writeValueAsString(request.getPortfolioLinks()));
            } catch (Exception ignored) {}
        }

        if (request.getProvinceCity() != null) blind.setProvinceCity(request.getProvinceCity());
        if (request.getWorkMode() != null) blind.setWorkMode(request.getWorkMode());
        if (request.getExperienceLevel() != null) blind.setExperienceLevel(request.getExperienceLevel());
        if (request.getExpectedSalaryMin() != null) blind.setExpectedSalaryMin(request.getExpectedSalaryMin());
        if (request.getExpectedSalaryMax() != null) blind.setExpectedSalaryMax(request.getExpectedSalaryMax());
        if (request.getPrimaryTechStack() != null) {
            try {
                blind.setPrimaryTechStack(objectMapper.writeValueAsString(request.getPrimaryTechStack()));
            } catch (Exception ignored) {}
        }

        // Sanitization using Strategy Pattern (BR-03)
        if (request.getBio() != null) {
            String sanitized = aiSanitizerService.sanitizeText(request.getBio());
            blind.setSanitizedBio(sanitized);
        }

        identityVaultRepository.save(vault);
        blindProfileRepository.save(blind);
    }
}
