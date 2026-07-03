package com.role.net.tripmaker.dto.expense;

import gogather.framework.billing.dto.DebtStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDebtStatusRequest(
    @NotNull(message = "Status é obrigatório.") DebtStatus status
) {}
