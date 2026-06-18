package com.role.net.gogather.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.dto.expense.ExpenseAutoCreationRequest;
import com.role.net.gogather.dto.expense.ExpenseContributionRequest;
import com.role.net.gogather.dto.expense.ExpenseDistributionRequest;
import com.role.net.gogather.dto.expense.ExpenseManualCreationRequest;
import com.role.net.gogather.dto.expense.ExpenseResponse;
import com.role.net.gogather.entity.Expense;
import com.role.net.gogather.entity.ExpenseContribution;
import com.role.net.gogather.entity.ExpenseDistribution;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.GroupMember;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.ExpenseStatus;
import com.role.net.gogather.enums.GroupRole;
import com.role.net.gogather.enums.SplitStatus;
import com.role.net.gogather.exception.InvalidDataException;
import com.role.net.gogather.exception.InvalidRequestException;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.exception.UnauthorizedRequestException;
import com.role.net.gogather.repository.ExpenseContributionRepository;
import com.role.net.gogather.repository.ExpenseDistributionRepository;
import com.role.net.gogather.repository.ExpenseRepository;
import com.role.net.gogather.repository.GroupRepository;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseContributionRepository expenseContributionRepository;
    private final ExpenseDistributionRepository expenseDistributionRepository;
    private final GroupRepository groupRepository;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        ExpenseContributionRepository expenseContributionRepository,
        ExpenseDistributionRepository expenseDistributionRepository,
        GroupRepository groupRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseContributionRepository = expenseContributionRepository;
        this.expenseDistributionRepository = expenseDistributionRepository;
        this.groupRepository = groupRepository;
    }

    private record MemberValue(GroupMember member, Long cents) {}

    public Expense createAuto(
        User requester,
        UUID groupExternalId,
        ExpenseAutoCreationRequest request
    ) {
        Long totalContributions = request.contributions().stream()
                .mapToLong(c -> toCents(c.value())).sum();
        if(!totalContributions.equals(toCents(request.totalValue())))
            throw new InvalidDataException("A soma das contribuições não bate com o valor total da despesa");

        Group group = groupRepository.findByExternalId(groupExternalId)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));

        GroupMember requesterMember = group.getMembers().stream()
            .filter(m -> m.getUser().equals(requester))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedRequestException("Você não faz parte deste grupo."));
        if (requesterMember.getRole() == GroupRole.MEMBER)
            throw new UnauthorizedRequestException("Você deve ser administrador para fazer isso.");

        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setGroup(group);
        expense.setStatus(ExpenseStatus.PENDING);

        int membersAmount = group.getMembers().size();
        Long valueInCents = toCents(request.totalValue());
        Long individualQuota = valueInCents / membersAmount;
        int remainingCents = Math.toIntExact(valueInCents % membersAmount);

        expense.setTotalValue(valueInCents);

        List<MemberValue> receivers = new ArrayList<>();
        List<MemberValue> payers = new ArrayList<>();

        buildLists(receivers, payers, group, request, individualQuota, remainingCents);
        putDistributionsAuto(receivers, payers, expense);
        putContributionsAuto(expense, group.getMembers(), request.contributions());

        return expenseRepository.save(expense);
    }

    public Expense createManual(
        User requester,
        UUID groupExternalId,
        ExpenseManualCreationRequest request
    ) {
        Long totalContributions = request.contributions().stream()
                .mapToLong(c -> toCents(c.value())).sum();
        if(!totalContributions.equals(toCents(request.totalValue())))
            throw new InvalidDataException("A soma das contribuições não bate com o valor total da despesa");

        Long totalDistributions = request.distributions().stream()
                .mapToLong(d -> toCents(d.value())).sum();
        if(!totalDistributions.equals(toCents(request.totalValue())))
            throw new InvalidDataException("A soma das distribuições não bate com o valor total da despesa");

        Group group = groupRepository.findByExternalId(groupExternalId)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));

        GroupMember requesterMember = group.getMembers().stream()
            .filter(m -> m.getUser().equals(requester))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedRequestException("Você não faz parte deste grupo."));
        if (requesterMember.getRole() == GroupRole.MEMBER)
            throw new UnauthorizedRequestException("Você deve ser administrador para fazer isso.");

        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setGroup(group);
        expense.setTotalValue(toCents(request.totalValue()));
        expense.setStatus(ExpenseStatus.PENDING);

        Map<UUID, GroupMember> members = group.getMembers().stream()
            .collect(Collectors.toMap(
                member -> member.getUser().getExternalId(),
                member -> member
            ));

        putContributionsManual(expense, members, request.contributions());
        putDistributionsManual(expense, members, request.distributions());

        return expenseRepository.save(expense);
    }

    public Expense findByExternalId(UUID externalId) {
        return expenseRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    public List<ExpenseResponse> getGroupExpenses(UUID groupExternalId, Long userId) {
        // Here we could verify if the user is a member of the group.
        // We'll rely on the GroupService or the controller to do this if needed,
        // or just fetch expenses directly.
        List<Expense> expenses = expenseRepository.findByGroup_ExternalId(groupExternalId);
        return expenses.stream()
            .map(ExpenseResponse::from)
            .collect(Collectors.toList());
    }

    public ExpenseDistribution findExpenseDistributionByExternalId(UUID externalId) {
        return expenseDistributionRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Parte de dívida não encontrada."));
    }

    @Transactional
    public void markAsPaid(Long requesterId, UUID externalId) {
        ExpenseDistribution expenseDistribution = expenseDistributionRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Parte de dívida não encontrada."));

        if(!expenseDistribution.getDebtor().getUser().getId().equals(requesterId))
            throw new UnauthorizedRequestException("Essa dívida não é sua!");
        if(!expenseDistribution.getStatus().equals(SplitStatus.PENDING))
            throw new InvalidRequestException("A dívida não está pendente.");

        expenseDistribution.setStatus(SplitStatus.AWAITING_CONFIRMATION);
    }

    @Transactional
    public void confirmReceipt(Long requesterId, UUID externalId) {
        ExpenseDistribution expenseDistribution = expenseDistributionRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Parte de dívida não encontrada."));

        if(!expenseDistribution.getCreditor().getUser().getId().equals(requesterId))
            throw new UnauthorizedRequestException("Você não é credor dessa dívida");
        if(!expenseDistribution.getStatus().equals(SplitStatus.AWAITING_CONFIRMATION))
            throw new InvalidRequestException("A dívida não aguardando confirmação.");

        expenseDistribution.setStatus(SplitStatus.SETTLED);

        if(!expenseDistributionRepository.existsByParentExpense_IdAndStatus(expenseDistribution.getParentExpense().getId(), SplitStatus.PENDING)
            && !expenseDistributionRepository.existsByParentExpense_IdAndStatus(expenseDistribution.getParentExpense().getId(), SplitStatus.AWAITING_CONFIRMATION)
        ) {
            expenseDistribution.getParentExpense().setStatus(ExpenseStatus.FINISHED);
        }
    }

    private void putContributionsManual(
        Expense expense,
        Map<UUID, GroupMember> members,
        List<ExpenseContributionRequest> contributions
    ) {
        for(ExpenseContributionRequest contributor : contributions) {

            GroupMember payer = members.get(contributor.payerExternalId());

            if(payer == null) {
                throw new InvalidDataException("Contribuidor não está no grupo.");
            }

            if(payer.getUser().getPixInfo() == null) {
                throw new InvalidRequestException(
                    "O membro " + payer.getUser().getDisplayName() + " não possui chave pix cadastrada."
                );
            }

            expense.getExpenseContributions().add(
                new ExpenseContribution(
                    toCents(contributor.value()),
                    expense,
                    payer
                )
            );
        }
    }

    private void putDistributionsManual(
        Expense expense,
        Map<UUID, GroupMember> members,
        List<ExpenseDistributionRequest> distributions
    ) {
        for(ExpenseDistributionRequest distribution : distributions) {

            if (distribution.debtorExternalId().equals(distribution.creditorExternalId()))
                throw new InvalidDataException("O devedor e o credor não podem ser a mesma pessoa.");

            GroupMember debtor = members.get(distribution.debtorExternalId());
            GroupMember creditor = members.get(distribution.creditorExternalId());

            if(debtor == null) {
                throw new InvalidDataException("O devedor não está no grupo.");
            }

            if(creditor == null) {
                throw new InvalidDataException("O credor não está no grupo.");
            }

            expense.getExpenseDistributions().add(
                new ExpenseDistribution(
                    toCents(distribution.value()),
                    SplitStatus.PENDING,
                    debtor,
                    creditor,
                    expense)
            );
        }
    }

    private void putDistributionsAuto(
        List<MemberValue> receivers,
        List<MemberValue> payers,
        Expense expense
    ) {
        int payersIndex = 0;
        int receiversIndex = 0;

        while(receiversIndex < receivers.size() && payersIndex < payers.size()) {

            Long receiverRemaining = receivers.get(receiversIndex).cents();
            Long payerRemaining = payers.get(payersIndex).cents();
            GroupMember payerMember = payers.get(payersIndex).member();
            GroupMember receieverMember = receivers.get(receiversIndex).member();

            if(payerRemaining <= receiverRemaining) {
                expense.getExpenseDistributions().add(
                    new ExpenseDistribution(
                        payerRemaining,
                        SplitStatus.PENDING,
                        payerMember,
                        receieverMember,
                        expense
                    )
                );
                if(payerRemaining.equals(receiverRemaining)) {
                    receiversIndex++;
                    payersIndex++;
                } else {
                    payersIndex++;
                    receivers.set(
                        receiversIndex,
                        new MemberValue(
                            receivers.get(receiversIndex).member(),
                            receiverRemaining - payerRemaining
                        )
                    );
                }
            } else {
                expense.getExpenseDistributions().add(
                    new ExpenseDistribution(
                        receiverRemaining,
                        SplitStatus.PENDING,
                        payerMember,
                        receieverMember,
                        expense
                    )
                );
                receiversIndex++;
                payers.set(
                    payersIndex,
                    new MemberValue(
                        payers.get(payersIndex).member(),
                        payerRemaining - receiverRemaining
                    )
                );
            }
        }
    }

    private void buildLists(
        List<MemberValue> receivers,
        List<MemberValue> payers,
        Group group,
        ExpenseAutoCreationRequest request,
        Long individualQuota,
        int remainingCents
    ) {
        Map<UUID, Long> paidAmount = request.contributions().stream()
            .collect(Collectors.toMap(
                ExpenseContributionRequest::payerExternalId,
                c -> Math.round(c.value() * 100),
                Long::sum
            ));

        for(GroupMember member : group.getMembers()) {

            Long memberQuota = individualQuota;

            if(remainingCents > 0) {
                memberQuota++;
                remainingCents--;
            }

            Long paidValue = paidAmount.getOrDefault(member.getUser().getExternalId(), 0L);
            Long balance = paidValue - memberQuota;

            if(balance > 0) {
                receivers.add(new MemberValue(member, Math.abs(balance)));
            } else if(balance < 0) {
                payers.add(new MemberValue(member, Math.abs(balance)));
            }
        }
    }

    private void putContributionsAuto(
        Expense expense,
        Set<GroupMember> groupMembers,
        List<ExpenseContributionRequest> contributionRequests
    ) {

        Map<UUID, GroupMember> membersInMemory = groupMembers.stream()
            .collect(Collectors.toMap(member -> member.getUser().getExternalId(), member -> member));

        for (ExpenseContributionRequest request : contributionRequests) {
            GroupMember payer = membersInMemory.get(request.payerExternalId());

            if(payer == null) {
                throw new InvalidDataException(
                    "O membro com ID " + request.payerExternalId() + " não pertence a este grupo."
                );
            }

            if(payer.getUser().getPixInfo() == null) {
                throw new InvalidRequestException(
                    "O membro " + payer.getUser().getDisplayName() + " não possui chave pix cadastrada."
                );
            }

            Long valueInCents = Math.round(request.value() * 100);

            expense.getExpenseContributions().add(
                new ExpenseContribution(valueInCents, expense, payer)
            );
        }
    }

    private Long toCents(Double value) {
        if(value == null) return 0L;
        return Math.round(value*100);
    }
}
