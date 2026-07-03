package com.role.net.tripmaker.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "Refresh token não pode ficar em branco!")
    String refreshToken
) {}
