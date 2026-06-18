package com.role.net.gogather.dto.expense;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ExpenseManualCreationRequest(
    String description,

    @NotNull(message = "Expense total value is missing.")
    Double totalValue,

    @NotEmpty(message = "Expense must have contributions.")
    List<ExpenseContributionRequest> contributions,

    @NotEmpty(message = "Expense must have distributions.")
    List<ExpenseDistributionRequest> distributions
) {
}
