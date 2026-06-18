package com.role.net.gogather.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventStopRequest(
    @NotBlank(message = "The name of the stop is required")
    String name,

    @NotNull(message = "The latitude is required")
    Double latitude,

    @NotNull(message = "The longitude is required")
    Double longitude,

    String category,

    @NotNull(message = "The order of the stop is required")
    Integer order,

    String city,

    String state
) {}
