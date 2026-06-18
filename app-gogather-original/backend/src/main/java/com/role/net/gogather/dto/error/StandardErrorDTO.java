package com.role.net.gogather.dto.error;

import java.time.Instant;

public record StandardErrorDTO(
    Instant timestamp,
    Integer status,
    String error,
    String message,
    String path
) {}
