package com.role.net.roomiesapp.dto.household;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateHouseholdRequest(
    @NotBlank(message = "O endereço é obrigatório.") String address,
    @NotNull(message = "O valor do aluguel é obrigatório.")
    @Min(value = 0, message = "O aluguel não pode ser negativo.") Long monthlyRentCents
) {}
