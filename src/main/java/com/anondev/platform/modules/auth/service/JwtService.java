package com.anondev.platform.modules.auth.service;

import io.jsonwebtoken.Claims;

import java.util.UUID;
import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(UUID userId, String email, String role);

    boolean isTokenValid(String token, String username);
}
