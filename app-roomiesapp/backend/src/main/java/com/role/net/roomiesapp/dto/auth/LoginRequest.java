package com.role.net.roomiesapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "O identificador (username ou email) é obrigatório.")
    String username,

    @NotBlank(message = "A senha é obrigatória.")
    String password
) {}
