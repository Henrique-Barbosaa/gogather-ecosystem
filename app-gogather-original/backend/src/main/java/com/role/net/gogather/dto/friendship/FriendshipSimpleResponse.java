package com.role.net.gogather.dto.friendship;

import java.util.UUID;

import com.role.net.gogather.entity.Friendship;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.FriendshipStatus;

public record FriendshipSimpleResponse(
    UUID fsExternalId,
    UUID friendExternalId,
    String friendUsername,
    String friendDisplayName,
    FriendshipStatus status
) {
    public static FriendshipSimpleResponse from(User user, Friendship fs) {
        return new FriendshipSimpleResponse(
            fs.getExternalId(),
            user.getExternalId(),
            user.getUsername(),
            user.getDisplayName(),
            fs.getStatus()
        );
    }
}
