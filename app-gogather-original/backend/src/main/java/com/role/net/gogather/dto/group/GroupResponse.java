package com.role.net.gogather.dto.group;

import java.time.Instant;

public record GroupResponse(
    String id,
    String name,
    String description,
    String inviteCode,
    Instant eventDate,
    Integer memberAmount
) {}