package com.role.net.roomiesapp.dto.auth;

public record TokenResponse(
    String refreshToken,
    String accessToken
) {}
