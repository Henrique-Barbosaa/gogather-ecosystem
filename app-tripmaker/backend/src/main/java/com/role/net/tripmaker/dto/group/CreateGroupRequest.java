package com.role.net.tripmaker.dto.group;

import java.time.LocalDate;

public record CreateGroupRequest(
    String name,
    String description,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    Integer maxTravelers,
    String coverUrl
) {}
