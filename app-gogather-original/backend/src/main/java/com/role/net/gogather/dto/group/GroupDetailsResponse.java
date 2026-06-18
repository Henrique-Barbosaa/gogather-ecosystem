package com.role.net.gogather.dto.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.role.net.gogather.enums.GroupRole;

public record GroupDetailsResponse(
    UUID externalId,
    String name,
    String description,
    String inviteCode,
    Instant createdAt,
    Instant eventDate,
    List<MemberDTO> members,
    List<EventStopDTO> eventStops
) {
    public record MemberDTO(
		UUID externalId,
		String username,
		String displayName,
		GroupRole role,
		String email
	) {}

    public record EventStopDTO(
        String name,
        Double latitude,
        Double longitude,
        String category,
        Integer stopOrder,
        String city,
        String state,
        String placeId
    ) {}
}
