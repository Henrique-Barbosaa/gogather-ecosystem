package com.role.net.roomiesapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username ou email não pode ficar em branco!")
    String username,

    @NotBlank(message = "Senha não pode ficar em branco!")
    String password
) {}
