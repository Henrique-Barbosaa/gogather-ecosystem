package com.role.net.tripmaker.dto.user;

import java.time.LocalDate;
import com.role.net.tripmaker.entity.User;

public record UserResponse(
    String id,
    String username,
    String name,
    String email,
    LocalDate birthDate,
    String pixKey
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getBirthDate(),
            user.getPixInfo() != null ? user.getPixInfo().getPixKey() : null
        );
    }
}
