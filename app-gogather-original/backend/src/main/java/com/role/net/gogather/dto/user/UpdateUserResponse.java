package com.role.net.gogather.dto.user;

import java.time.LocalDate;

import com.role.net.gogather.entity.User;

public record UpdateUserResponse(
    String username,
    String displayName,
    String email,
    LocalDate birthDate
) {
    public static UpdateUserResponse from(User user) {
        return new UpdateUserResponse(
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getBirthDate());
    }
}
