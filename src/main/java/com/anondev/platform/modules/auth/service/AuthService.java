package com.anondev.platform.modules.auth.service;

import com.anondev.platform.common.BusinessException;
import com.anondev.platform.modules.auth.dto.AuthResponse;
import com.anondev.platform.modules.auth.dto.LoginRequest;
import com.anondev.platform.modules.auth.dto.RegisterCandidateRequest;
import com.anondev.platform.modules.auth.dto.RegisterEmployerRequest;
import com.anondev.platform.modules.auth.entity.User;
import com.anondev.platform.modules.auth.repository.UserRepository;
import com.anondev.platform.modules.candidate.entity.CandidateBlindProfile;
import com.anondev.platform.modules.candidate.entity.CandidateIdentityVault;
import com.anondev.platform.modules.candidate.repository.BlindProfileRepository;
import com.anondev.platform.modules.candidate.repository.IdentityVaultRepository;
import com.anondev.platform.modules.company.entity.Company;
import com.anondev.platform.modules.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BlindProfileRepository blindProfileRepository;
    private final IdentityVaultRepository identityVaultRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registerCandidate(RegisterCandidateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("CANDIDATE")
                .accountStatus("ACTIVE")
                .build();
        user = userRepository.save(user);

        // Auto-generate candidate code CAND-xxxxx
        String candidateCode = "CAND-" + String.format("%05d", new Random().nextInt(90000) + 10000);

        CandidateBlindProfile blindProfile = CandidateBlindProfile.builder()
                .candidateId(user.getId())
                .user(user)
                .candidateCode(candidateCode)
                .provinceCity(request.getProvinceCity() != null ? request.getProvinceCity() : "Toàn quốc")
                .workMode(request.getWorkMode() != null ? request.getWorkMode() : "HYBRID")
                .build();
        blindProfileRepository.save(blindProfile);

        CandidateIdentityVault vault = CandidateIdentityVault.builder()
                .candidateId(user.getId())
                .user(user)
                .realFullName(request.getRealFullName())
                .contactEmail(request.getEmail())
                .contactPhone(request.getPhone())
                .originalCvUrl(request.getOriginalCvUrl() != null ? request.getOriginalCvUrl() : "")
                .build();
        identityVaultRepository.save(vault);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .candidateCode(candidateCode)
                .build();
    }

    @Transactional
    public AuthResponse registerEmployer(RegisterEmployerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already registered");
        }
        if (companyRepository.existsByTaxId(request.getTaxId())) {
            throw new BusinessException("Tax ID already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("EMPLOYER")
                .accountStatus("ACTIVE")
                .build();
        user = userRepository.save(user);

        Company company = Company.builder()
                .ownerUserId(user.getId())
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .isTaxVerified(false)
                .build();
        company = companyRepository.save(company);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(company.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }

        String candidateCode = null;
        if ("CANDIDATE".equals(user.getRole())) {
            candidateCode = blindProfileRepository.findById(user.getId())
                    .map(CandidateBlindProfile::getCandidateCode)
                    .orElse(null);
        }

        var company = companyRepository.findByOwnerUserId(user.getId());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .candidateCode(candidateCode)
                .companyId(company.map(Company::getId).orElse(null))
                .build();
    }
}
