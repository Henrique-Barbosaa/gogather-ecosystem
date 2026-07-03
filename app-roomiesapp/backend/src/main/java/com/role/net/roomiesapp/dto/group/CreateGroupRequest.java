package com.role.net.roomiesapp.dto.group;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
    @NotBlank(message = "O nome do grupo/casa é obrigatório") String name,
    String description,
    String address,
    Long monthlyRentCents,
    Integer maxOccupants
) {}
