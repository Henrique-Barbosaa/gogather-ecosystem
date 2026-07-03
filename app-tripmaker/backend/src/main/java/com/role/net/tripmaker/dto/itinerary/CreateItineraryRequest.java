package com.role.net.tripmaker.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateItineraryRequest(
    @NotBlank(message = "O título do evento é obrigatório.") String title,
    String description,
    @NotNull(message = "O início do evento é obrigatório.") LocalDateTime startTime,
    @NotNull(message = "O fim do evento é obrigatório.") LocalDateTime endTime,
    String location,
    Long costEstimateCents
) {}
