package com.role.net.tripmaker.dto.group;

import com.role.net.tripmaker.entity.Group;
import java.time.LocalDate;

public record GroupResponse(
    Long id,
    String inviteCode,
    String name,
    String description,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    Integer maxTravelers
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
            group.getId(),
            group.getInviteCode(),
            group.getName(),
            group.getDescription(),
            group.getDestination(),
            group.getStartDate(),
            group.getEndDate(),
            group.getMaxTravelers()
        );
    }
}
