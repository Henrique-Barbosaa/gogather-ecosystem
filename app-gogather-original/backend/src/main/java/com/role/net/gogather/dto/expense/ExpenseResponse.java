package com.role.net.gogather.dto.expense;

import java.util.List;
import java.util.UUID;

import com.role.net.gogather.entity.Expense;

public record ExpenseResponse(
    UUID expenseExternalId,
    String description,
    Double totalValue,
    List<ExpenseContributionResponse> contributions,
    List<ExpenseDistributionResponse> distributions
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getExternalId(),
            expense.getDescription(),
            expense.getTotalValue()/100.0,
            expense.getExpenseContributions().stream()
                .map(ExpenseContributionResponse::from)
                .toList(),
            expense.getExpenseDistributions().stream()
                .map(ExpenseDistributionResponse::from)
                .toList()
        );
    }
}
