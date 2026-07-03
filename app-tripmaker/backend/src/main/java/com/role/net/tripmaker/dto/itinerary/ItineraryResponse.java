package com.role.net.tripmaker.dto.itinerary;

import com.role.net.tripmaker.entity.ItineraryEvent;
import java.time.LocalDateTime;

public record ItineraryResponse(
    Long id,
    String title,
    String description,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location,
    Long costEstimateCents,
    String creatorUsername
) {
    public static ItineraryResponse from(ItineraryEvent event) {
        return new ItineraryResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartTime(),
            event.getEndTime(),
            event.getLocation(),
            event.getCostEstimateCents(),
            event.getCreator() != null ? event.getCreator().getUsername() : null
        );
    }
}
