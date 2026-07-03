package com.role.net.tripmaker.dto.packing;

import jakarta.validation.constraints.NotBlank;

public record CreatePackingItemRequest(
    @NotBlank(message = "O nome do item é obrigatório.") String name
) {}
