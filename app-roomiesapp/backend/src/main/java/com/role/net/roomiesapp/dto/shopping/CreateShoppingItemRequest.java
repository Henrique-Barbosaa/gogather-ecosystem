package com.role.net.roomiesapp.dto.shopping;

import jakarta.validation.constraints.NotBlank;

public record CreateShoppingItemRequest(
    @NotBlank(message = "O nome do item é obrigatório.") String name,
    String quantity
) {}
