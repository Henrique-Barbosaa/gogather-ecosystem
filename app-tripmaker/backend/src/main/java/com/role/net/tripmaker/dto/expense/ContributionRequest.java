package com.role.net.tripmaker.dto.expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContributionRequest(
    @NotNull(message = "ID do usuário é obrigatório.") Long userId,
    @NotNull(message = "Valor é obrigatório.") @Positive(message = "Valor deve ser maior que zero.") Long amountInCents
) {}
