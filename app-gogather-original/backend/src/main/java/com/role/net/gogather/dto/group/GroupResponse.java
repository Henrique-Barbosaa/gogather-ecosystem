package com.role.net.gogather.dto.group;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
    UUID externalId,
    String name,
    String description,
    String inviteCode,
  	Instant eventDate,
    Integer memberAmount
) {}
