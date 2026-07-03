package com.role.net.tripmaker.dto.trip;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UpdateTripRequest(
    @NotBlank(message = "O destino é obrigatório.") String destination,
    LocalDate startDate,
    LocalDate endDate
) {}
