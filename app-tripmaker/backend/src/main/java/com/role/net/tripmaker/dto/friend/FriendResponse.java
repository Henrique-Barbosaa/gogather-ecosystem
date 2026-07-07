package com.role.net.tripmaker.dto.friend;

public record FriendResponse(
    Long id,
    Long friendUserId,
    String name,
    String email,
    String avatar,
    String status,
    String requestDirection,
    int sharedTripsCount
) {}
