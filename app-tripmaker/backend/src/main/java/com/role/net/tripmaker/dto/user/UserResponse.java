package com.role.net.tripmaker.dto.user;

import java.time.LocalDate;
import com.role.net.tripmaker.entity.User;

public record UserResponse(
    String id,
    String username,
    String displayName,
    String email,
    LocalDate birthDate,
    String pixKey
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getExternalId() != null ? user.getExternalId().toString() : String.valueOf(user.getId()),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getBirthDate(),
            user.getPixInfo() != null ? user.getPixInfo().getPixKey() : null
        );
    }
}
