package com.role.net.gogather.dto.expense;

import java.util.UUID;

import com.role.net.gogather.entity.ExpenseDistribution;
import com.role.net.gogather.enums.SplitStatus;

public record ExpenseDistributionResponse(
    UUID expenseDistributionExternalId,
    Double value,
    SplitStatus status,
    UUID debtorExternalId,
    UUID creditorExternalId,
    UUID parentExpenseExternalId
) {
    public static ExpenseDistributionResponse from(ExpenseDistribution expenseDistribution) {
        return new ExpenseDistributionResponse(
            expenseDistribution.getExternalId(),
            expenseDistribution.getValue()/100.0,
            expenseDistribution.getStatus(),
            expenseDistribution.getDebtor().getUser().getExternalId(),
            expenseDistribution.getCreditor().getUser().getExternalId(),
            expenseDistribution.getParentExpense().getExternalId()
        );
    }
}
