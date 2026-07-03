package com.role.net.gogather.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.entity.Expense;
import com.role.net.gogather.entity.ExpenseDistribution;
import gogather.framework.group.jpa.domain.GroupMember;
import gogather.framework.billing.dto.DebtStatus;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.ExpenseDistributionRepository;
import com.role.net.gogather.repository.ExpenseRepository;

import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.core.Participant;

@Service
public class AppBillingDataProvider implements BillingDataProvider {

    private final ExpenseRepository expenseRepository;
    private final ExpenseDistributionRepository expenseDistributionRepository;

    public AppBillingDataProvider(
        ExpenseRepository expenseRepository,
        ExpenseDistributionRepository expenseDistributionRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseDistributionRepository = expenseDistributionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCents(String expenseId) {
        Expense expense = findExpenseById(expenseId);
        return expense.getTotalValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String expenseId) {
        Expense expense = findExpenseById(expenseId);
        return new ArrayList<>(expense.getGroup().getMembers());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contribution> getContributions(String expenseId) {
        Expense expense = findExpenseById(expenseId);
        return expense.getExpenseContributions().stream()
            .map(ec -> new Contribution(ec.getPayer(), ec.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveDistributions(String expenseId, List<DebtDistribution> distributions) {
        Expense expense = findExpenseById(expenseId);
        List<ExpenseDistribution> entityList = new ArrayList<>();

        for (DebtDistribution dist : distributions) {
            GroupMember debtor = (GroupMember) dist.debtor();
            GroupMember creditor = (GroupMember) dist.creditor();

            DebtStatus status = dist.status() != null ? dist.status() : DebtStatus.PENDING;
            ExpenseDistribution ed = new ExpenseDistribution(
                dist.amountInCents(),
                status,
                debtor,
                creditor,
                expense
            );
            expense.getExpenseDistributions().add(ed);
            entityList.add(ed);
        }

        expenseDistributionRepository.saveAll(entityList);
    }

    private Expense findExpenseById(String expenseId) {
        try {
            Long id = Long.valueOf(expenseId);
            return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada com ID: " + expenseId));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("ID da despesa inválido: " + expenseId);
        }
    }
}
