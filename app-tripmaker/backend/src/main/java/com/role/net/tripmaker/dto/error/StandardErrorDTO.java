package com.role.net.tripmaker.dto.error;

import java.time.Instant;

public record StandardErrorDTO(
    Instant timestamp,
    Integer status,
    String error,
    String message,
    String path
) {}
