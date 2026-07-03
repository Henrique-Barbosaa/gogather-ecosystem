package com.role.net.roomiesapp.dto.group;

public record CreateGroupRequest(
    String name,
    String description,
    String address,
    Long monthlyRentCents,
    Integer maxOccupants
) {
}
