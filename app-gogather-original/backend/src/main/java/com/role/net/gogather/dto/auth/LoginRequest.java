package com.role.net.gogather.dto.auth;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
    @NotEmpty(message = "Username required") String username,
    @NotEmpty(message = "Password required") String password
) {}
