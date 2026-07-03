package com.role.net.tripmaker.dto.expense;

import com.role.net.tripmaker.entity.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateExpenseRequest(
    @NotBlank(message = "Descrição é obrigatória.") String description,
    @NotNull(message = "Data da despesa é obrigatória.") LocalDate expenseDate,
    ExpenseCategory category,
    @NotEmpty(message = "Pelo menos um contribuinte deve ser informado.") List<ContributionRequest> contributions,
    List<Long> participantIds
) {}
