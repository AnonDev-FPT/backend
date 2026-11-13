package com.anondev.platform.modules.auth.controller;

import com.anondev.platform.common.ApiResponse;
import com.anondev.platform.modules.auth.dto.AuthResponse;
import com.anondev.platform.modules.auth.dto.LoginRequest;
import com.anondev.platform.modules.auth.dto.RegisterCandidateRequest;
import com.anondev.platform.modules.auth.dto.RegisterEmployerRequest;
import com.anondev.platform.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-candidate")
    @Operation(summary = "Register Candidate")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCandidate(@Valid @RequestBody RegisterCandidateRequest request) {
        AuthResponse response = authService.registerCandidate(request);
        return ResponseEntity.ok(ApiResponse.ok("Candidate registered successfully", response));
    }

    @PostMapping("/register-employer")
    @Operation(summary = "Register Employer")
    public ResponseEntity<ApiResponse<AuthResponse>> registerEmployer(@Valid @RequestBody RegisterEmployerRequest request) {
        AuthResponse response = authService.registerEmployer(request);
        return ResponseEntity.ok(ApiResponse.ok("Employer registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
