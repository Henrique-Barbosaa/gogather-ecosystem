package com.role.net.gogather.dto.expense;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ExpenseAutoCreationRequest(
    String description,

    @NotNull(message = "Expense total value is missing.")
    Double totalValue,

    @NotEmpty(message = "Expense must have at least one contribution.")
    List<ExpenseContributionRequest> contributions
) {
}
