package com.role.net.gogather.dto.auth;

import jakarta.validation.constraints.NotEmpty;

public record RefreshRequest(@NotEmpty(message = "RefreshToken required") String refreshToken) {
}
