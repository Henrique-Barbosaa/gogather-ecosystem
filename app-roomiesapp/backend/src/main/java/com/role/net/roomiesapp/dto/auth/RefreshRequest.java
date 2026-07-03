package com.role.net.roomiesapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "Refresh token não pode ficar em branco!")
    String refreshToken
) {}
