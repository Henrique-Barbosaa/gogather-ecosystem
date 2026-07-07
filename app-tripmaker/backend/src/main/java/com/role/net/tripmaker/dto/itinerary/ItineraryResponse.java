package com.role.net.tripmaker.dto.itinerary;

import com.role.net.tripmaker.entity.ItineraryEvent;
import java.time.LocalDateTime;

public record ItineraryResponse(
    String id,
    String title,
    String description,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location,
    Long costEstimateCents,
    String creatorUsername,
    Integer sequenceOrder,
    String day,
    String time,
    String category,
    String groupId
) {
    public static ItineraryResponse from(ItineraryEvent event) {
        String dayStr = event.getDay() != null ? event.getDay() : (event.getStartTime() != null ? "Dia " + event.getStartTime().getDayOfMonth() : "Dia 1");
        String timeStr = event.getTime() != null ? event.getTime() : (event.getStartTime() != null ? String.format("%02d:%02d", event.getStartTime().getHour(), event.getStartTime().getMinute()) : "12:00");
        String categoryStr = event.getCategory() != null ? event.getCategory() : "Passeio";
        String groupIdStr = event.getGroup() != null ? String.valueOf(event.getGroup().getId()) : "";
        return new ItineraryResponse(
            String.valueOf(event.getId()),
            event.getTitle(),
            event.getDescription(),
            event.getStartTime(),
            event.getEndTime(),
            event.getLocation() != null ? event.getLocation() : "Local a definir",
            event.getCostEstimateCents(),
            event.getCreator() != null ? event.getCreator().getUsername() : null,
            event.getSequenceOrder(),
            dayStr,
            timeStr,
            categoryStr,
            groupIdStr
        );
    }
}
