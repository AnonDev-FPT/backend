package com.anondev.platform.modules.auth.service;

import com.anondev.platform.modules.auth.dto.AuthResponse;
import com.anondev.platform.modules.auth.dto.LoginRequest;
import com.anondev.platform.modules.auth.dto.RegisterCandidateRequest;
import com.anondev.platform.modules.auth.dto.RegisterEmployerRequest;

public interface AuthService {

    AuthResponse registerCandidate(RegisterCandidateRequest request);

    AuthResponse registerEmployer(RegisterEmployerRequest request);

    AuthResponse login(LoginRequest request);
}
