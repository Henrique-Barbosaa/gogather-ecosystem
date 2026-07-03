package com.role.net.tripmaker.dto.auth;

public record RegisterUserResponse(
    String username,
    String email,
    String displayName
) {}
