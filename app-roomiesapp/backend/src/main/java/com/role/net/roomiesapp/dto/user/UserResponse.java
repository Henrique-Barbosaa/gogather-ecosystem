package com.role.net.roomiesapp.dto.user;

import java.time.LocalDate;
import com.role.net.roomiesapp.entity.User;

public record UserResponse(
    String id,
    String username,
    String name,
    String email,
    String phoneNumber,
    LocalDate birthDate
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getBirthDate()
        );
    }
}
