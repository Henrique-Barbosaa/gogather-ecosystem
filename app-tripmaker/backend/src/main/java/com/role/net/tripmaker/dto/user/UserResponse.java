package com.role.net.tripmaker.dto.user;

import java.time.LocalDate;
import com.role.net.tripmaker.entity.User;

public record UserResponse(
    String id,
    String username,
    String name,
    String email,
    LocalDate birthDate,
    String pixKey,
    String pixType,
    String bio,
    String avatar
) {
    public static UserResponse from(User user) {
        String nameStr = user.getName() != null ? user.getName() : "Viajante";
        String avatarStr = "https://ui-avatars.com/api/?name=" + nameStr.replace(" ", "+") + "&background=random&color=fff&size=256&font-size=0.4";
        return new UserResponse(
            String.valueOf(user.getId()),
            user.getUsername(),
            nameStr,
            user.getEmail(),
            user.getBirthDate(),
            user.getPixInfo() != null ? user.getPixInfo().getPixKey() : null,
            user.getPixInfo() != null ? user.getPixInfo().getPixType() : null,
            user.getBio(),
            avatarStr
        );
    }
}
