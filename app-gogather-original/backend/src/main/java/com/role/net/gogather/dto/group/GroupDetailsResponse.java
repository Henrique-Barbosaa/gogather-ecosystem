package com.role.net.gogather.dto.group;

import java.time.Instant;
import java.util.List;

public record GroupDetailsResponse(
    String id, // Alterado de UUID para String
    String name,
    String description,
    String inviteCode,
    java.time.LocalDateTime createdAt,
    Instant eventDate,
    List<MemberDTO> members,
    List<EventStopDTO> eventStops
) {
    public record MemberDTO(
        String id,
        String username,
        String displayName,
        String role,
        String email
    ) {}

    public record EventStopDTO(
        String id,
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