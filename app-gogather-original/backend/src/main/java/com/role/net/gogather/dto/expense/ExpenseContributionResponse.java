package com.role.net.gogather.dto.expense;

import java.util.UUID;

import com.role.net.gogather.entity.ExpenseContribution;

public record ExpenseContributionResponse(
    UUID expenseContributionExternalId,
    Double value,
    UUID payerExternalId,
    UUID parentExpenseExternalId
) {
    public static ExpenseContributionResponse from(ExpenseContribution expenseContribution) {
        return new ExpenseContributionResponse(
            expenseContribution.getExternalId(),
            expenseContribution.getValue()/100.0,
            expenseContribution.getPayer().getUser().getExternalId(),
            expenseContribution.getParentExpense().getExternalId()
        );
    }
}
