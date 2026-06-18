package com.role.net.gogather.dto.auth;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotEmpty(message = "Username required")
    @Size(min = 6, max = 32, message = "O username deve ter no mínimo 6 caracteres e no máximo 32")
    String username,

    String displayName,

    @Email @NotEmpty(message = "E-mail required")
    String email,

    @NotEmpty(message = "Password required")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    String password,

    @NotNull(message = "Birth date required")
    LocalDate birthDate
) {}
