package com.role.net.tripmaker.dto.expense;

import com.role.net.tripmaker.entity.ExpenseCategory;
import com.role.net.tripmaker.entity.TripExpense;
import java.time.LocalDate;
import java.util.List;

public record TripExpenseResponse(
    Long id,
    String description,
    Long totalCents,
    LocalDate expenseDate,
    ExpenseCategory category,
    List<ContributionResponse> contributions,
    List<ParticipantResponse> participants,
    List<TripDebtResponse> debts
) {
    public static TripExpenseResponse from(TripExpense expense) {
        return new TripExpenseResponse(
            expense.getId(),
            expense.getDescription(),
            expense.getTotalCents(),
            expense.getExpenseDate(),
            expense.getCategory(),
            expense.getContributions() != null ? expense.getContributions().stream().map(ContributionResponse::from).toList() : List.of(),
            expense.getParticipants() != null ? expense.getParticipants().stream().map(p -> new ParticipantResponse(p.getParticipant().getId(), p.getParticipant().getUsername(), p.getParticipant().getDisplayName())).toList() : List.of(),
            expense.getDebts() != null ? expense.getDebts().stream().map(TripDebtResponse::from).toList() : List.of()
        );
    }
}
