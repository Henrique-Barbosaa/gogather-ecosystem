package com.role.net.tripmaker.service.billing;

import com.role.net.tripmaker.entity.TripDebt;
import com.role.net.tripmaker.entity.TripExpense;
import com.role.net.tripmaker.entity.TripExpenseContribution;
import com.role.net.tripmaker.entity.TripExpenseParticipant;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.TripDebtRepository;
import com.role.net.tripmaker.repository.TripExpenseRepository;
import com.role.net.tripmaker.repository.UserRepository;
import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.core.Participant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripBillingDataProvider implements BillingDataProvider {

    private final TripExpenseRepository expenseRepository;
    private final TripDebtRepository debtRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCents(String expenseId) {
        TripExpense expense = findExpense(expenseId);
        return expense.getTotalCents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String expenseId) {
        TripExpense expense = findExpense(expenseId);
        if (expense.getParticipants() != null && !expense.getParticipants().isEmpty()) {
            return expense.getParticipants().stream()
                .map(TripExpenseParticipant::getParticipant)
                .map(u -> (Participant) u)
                .toList();
        }
        return expense.getTrip().getMembers().stream()
            .map(member -> (Participant) member.getUser())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contribution> getContributions(String expenseId) {
        TripExpense expense = findExpense(expenseId);
        return expense.getContributions().stream()
            .map(c -> new Contribution(c.getPayer(), c.getAmountInCents()))
            .toList();
    }

    @Override
    @Transactional
    public void saveDistributions(String expenseId, List<DebtDistribution> distributions) {
        TripExpense expense = findExpense(expenseId);
        debtRepository.deleteByExpenseId(expense.getId());
        if (expense.getDebts() != null) {
            expense.getDebts().clear();
        }

        for (DebtDistribution dist : distributions) {
            User debtor = userRepository.findById(Long.parseLong(dist.debtor().getIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("Devedor não encontrado: " + dist.debtor().getIdentifier()));
            User creditor = userRepository.findById(Long.parseLong(dist.creditor().getIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("Credor não encontrado: " + dist.creditor().getIdentifier()));

            TripDebt debt = TripDebt.builder()
                .expense(expense)
                .debtor(debtor)
                .creditor(creditor)
                .amountInCents(dist.amountInCents())
                .status(DebtStatus.PENDING)
                .build();
            TripDebt savedDebt = debtRepository.save(debt);
            if (expense.getDebts() != null) {
                expense.getDebts().add(savedDebt);
            }
        }
    }

    private TripExpense findExpense(String expenseId) {
        try {
            Long id = Long.parseLong(expenseId);
            return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + expenseId));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("ID da despesa inválido: " + expenseId);
        }
    }
}
