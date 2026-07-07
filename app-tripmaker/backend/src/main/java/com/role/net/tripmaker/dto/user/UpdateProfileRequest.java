package com.role.net.tripmaker.dto.user;

public record UpdateProfileRequest(
    String name,
    String bio,
    String email
) {}
