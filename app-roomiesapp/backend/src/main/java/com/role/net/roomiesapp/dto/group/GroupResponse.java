package com.role.net.roomiesapp.dto.group;

import com.role.net.roomiesapp.entity.Group;

public record GroupResponse(
    Long id,
    String inviteCode,
    String name,
    String description,
    String address,
    Long monthlyRentCents,
    Integer maxOccupants
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
            group.getId(),
            group.getInviteCode(),
            group.getName(),
            group.getDescription(),
            group.getAddress(),
            group.getMonthlyRentCents(),
            group.getMaxOccupants()
        );
    }
}
