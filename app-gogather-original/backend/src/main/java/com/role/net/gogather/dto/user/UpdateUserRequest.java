package com.role.net.gogather.dto.user;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRequest(
    String username,
    String displayName,
    String email,
    @NotEmpty(message = "Password required.") String password,
    String newPassword,
    LocalDate birthDate
) {
}
