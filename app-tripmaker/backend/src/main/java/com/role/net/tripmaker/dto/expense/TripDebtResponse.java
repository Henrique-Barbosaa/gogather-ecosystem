package com.role.net.tripmaker.dto.expense;

import com.role.net.tripmaker.entity.TripDebt;
import gogather.framework.billing.dto.DebtStatus;

public record TripDebtResponse(
    Long id,
    Long expenseId,
    String expenseDescription,
    Long debtorId,
    String debtorUsername,
    String debtorDisplayName,
    Long creditorId,
    String creditorUsername,
    String creditorDisplayName,
    Long amountInCents,
    DebtStatus status
) {
    public static TripDebtResponse from(TripDebt debt) {
        return new TripDebtResponse(
            debt.getId(),
            debt.getExpense().getId(),
            debt.getExpense().getDescription(),
            debt.getDebtor().getId(),
            debt.getDebtor().getUsername(),
            debt.getDebtor().getDisplayName(),
            debt.getCreditor().getId(),
            debt.getCreditor().getUsername(),
            debt.getCreditor().getDisplayName(),
            debt.getAmountInCents(),
            debt.getStatus()
        );
    }
}
