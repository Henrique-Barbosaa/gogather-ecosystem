package com.role.net.roomiesapp.dto.group;

import com.role.net.roomiesapp.entity.User;
import java.util.UUID;

public record GroupMemberResponse(
    Long id,
    UUID externalId,
    String username,
    String displayName
) {
    public static GroupMemberResponse from(User user) {
        return new GroupMemberResponse(
            user.getId(),
            user.getExternalId(),
            user.getUsername(),
            user.getDisplayName()
        );
    }
}
