package com.anondev.platform.modules.candidate.service;

import com.anondev.platform.modules.candidate.dto.UpdateProfileRequest;

import java.util.Map;
import java.util.UUID;

public interface CandidateProfileService {

    Map<String, Object> getCandidateVaultDataByUserEmail(String userEmail);

    void updateCandidateVaultByUserEmail(String userEmail, UpdateProfileRequest request);

    Map<String, Object> getCandidateVaultData(UUID candidateId);

    void updateCandidateVault(UUID candidateId, UpdateProfileRequest request);
}
