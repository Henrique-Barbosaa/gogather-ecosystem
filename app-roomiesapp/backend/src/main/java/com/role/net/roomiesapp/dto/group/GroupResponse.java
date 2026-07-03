package com.role.net.roomiesapp.dto.group;

import com.role.net.roomiesapp.entity.Group;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
    Long id,
    UUID externalId,
    String name,
    String description,
    String inviteCode,
    String address,
    Long monthlyRentCents,
    Integer maxOccupants,
    LocalDateTime createdAt
) {
    public static GroupResponse from(Group group) {
        if (group == null) return null;
        return new GroupResponse(
            group.getId(),
            group.getExternalId(),
            group.getName(),
            group.getDescription(),
            group.getInviteCode(),
            group.getAddress(),
            group.getMonthlyRentCents(),
            group.getMaxOccupants(),
            group.getCreatedAt()
        );
    }
}
