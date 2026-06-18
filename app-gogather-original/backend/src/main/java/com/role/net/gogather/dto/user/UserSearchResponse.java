package com.role.net.gogather.dto.user;

import com.role.net.gogather.entity.User;

public record UserSearchResponse(
    String externalId,
    String username,
    String displayName
) {
    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
            user.getExternalId().toString(),
            user.getUsername(),
            user.getDisplayName());
    }
}
