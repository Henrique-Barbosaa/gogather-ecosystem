package com.role.net.tripmaker.dto.auth;

public record TokenResponse(
    String refreshToken,
    String accessToken
) {}
