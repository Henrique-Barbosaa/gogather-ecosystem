package com.role.net.tripmaker.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateItineraryRequest(
    @NotBlank(message = "O título do evento é obrigatório.") String title,
    String description,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location,
    Long costEstimateCents,
    String day,
    String time,
    String category
) {}
