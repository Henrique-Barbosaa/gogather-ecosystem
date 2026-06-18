package com.role.net.gogather.dto.group;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
	@NotBlank(message = "Group name is required")
	@Size(min = 1, max = 255, message = "Group name must be between 1 and 255 characters")
	String name,

	@Size(max = 500, message = "Group description must be at most 500 characters")
	String description,

	@NotNull(message = "Event date is required")
	Instant date,

	@Size(min = 1, message = "At least one event stop is required")
	@NotNull(message = "Event stops cannot be null")
	@Valid
	List<EventStopRequest> stops
) {}
