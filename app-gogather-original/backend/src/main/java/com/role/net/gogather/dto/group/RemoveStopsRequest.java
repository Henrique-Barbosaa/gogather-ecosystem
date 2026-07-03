package com.role.net.gogather.dto.group;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RemoveStopsRequest(
    @NotNull(message = "A lista de IDs para remoção não pode ser nula")
    @Size(min = 1, message = "A lista deve conter ao menos uma parada")
    List<Long> stopIdsToRemove
) {}