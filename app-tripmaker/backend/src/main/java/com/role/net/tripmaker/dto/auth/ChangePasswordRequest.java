package com.role.net.tripmaker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Senha antiga não pode ficar em branco!")
    String oldPassword,

    @NotBlank(message = "Nova senha não pode ficar em branco!")
    @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres!")
    String newPassword
) {}
