package com.role.net.roomiesapp.dto.auth;

public record RegisterUserResponse(
    String username,
    String email,
    String displayName,
    String phoneNumber
) {}
