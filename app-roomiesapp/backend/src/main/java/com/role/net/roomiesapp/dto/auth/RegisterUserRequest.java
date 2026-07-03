package com.role.net.roomiesapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterUserRequest(
    @NotBlank(message = "Username não pode ficar em branco!")
    String username,

    @NotBlank(message = "E-mail não pode ficar em branco!")
    @Email(message = "Formato de e-mail inválido!")
    String email,

    @NotBlank(message = "Senha não pode ficar em branco!")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres!")
    String password,

    String displayName,

    @NotNull(message = "Data de nascimento não pode ser nula!")
    LocalDate birthDate
) {}
