package com.role.net.gogather.dto.expense;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ExpenseDistributionRequest(
    @NotNull(message = "Expense distribution value is missing.") Double value,
    @NotNull(message = "Expense distribution debtor id is missing.") UUID debtorExternalId,
    @NotNull(message = "Expense distribution creditor id is missing") UUID creditorExternalId
) {
}
