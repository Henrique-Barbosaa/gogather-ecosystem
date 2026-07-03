package com.role.net.tripmaker.service.billing;

import com.role.net.tripmaker.dto.expense.ContributionRequest;
import com.role.net.tripmaker.dto.expense.CreateExpenseRequest;
import com.role.net.tripmaker.dto.expense.PixCodeResponse;
import com.role.net.tripmaker.dto.expense.TripDebtResponse;
import com.role.net.tripmaker.dto.expense.TripExpenseResponse;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.TripDebt;
import com.role.net.tripmaker.entity.TripExpense;
import com.role.net.tripmaker.entity.TripExpenseContribution;
import com.role.net.tripmaker.entity.TripExpenseParticipant;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.TripDebtRepository;
import com.role.net.tripmaker.repository.TripExpenseRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.billing.orchestrator.BillingOrchestrator;
import gogather.framework.billing.pix.PixCodeGenerator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripBillingService {

    private final TripExpenseRepository expenseRepository;
    private final TripDebtRepository debtRepository;
    private final TripGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BillingOrchestrator billingOrchestrator;
    private final PixCodeGenerator pixCodeGenerator;

    @Transactional
    public TripExpenseResponse createExpense(Long tripId, CreateExpenseRequest request, User loggedUser) {
        Group trip = groupRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada."));

        if (!trip.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta viagem.");
        }

        long totalCents = request.contributions().stream()
            .mapToLong(ContributionRequest::amountInCents)
            .sum();

        TripExpense expense = TripExpense.builder()
            .trip(trip)
            .description(request.description())
            .totalCents(totalCents)
            .expenseDate(request.expenseDate())
            .category(request.category() != null ? request.category() : com.role.net.tripmaker.entity.ExpenseCategory.OUTROS)
            .contributions(new ArrayList<>())
            .participants(new ArrayList<>())
            .debts(new ArrayList<>())
            .build();

        for (ContributionRequest cr : request.contributions()) {
            User contributorUser = userRepository.findById(cr.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário contribuinte não encontrado: " + cr.userId()));

            if (!trip.hasMember(contributorUser.getId().toString())) {
                throw new IllegalArgumentException("O contribuinte " + contributorUser.getUsername() + " não é membro desta viagem.");
            }

            if (contributorUser.getPixInfo() == null || contributorUser.getPixInfo().getPixKey() == null) {
                throw new IllegalArgumentException("O contribuinte " + contributorUser.getUsername() + " não possui uma chave Pix cadastrada. É necessário cadastrar a chave Pix antes de adicioná-lo como contribuinte.");
            }

            TripExpenseContribution contribution = TripExpenseContribution.builder()
                .expense(expense)
                .payer(contributorUser)
                .amountInCents(cr.amountInCents())
                .build();
            expense.getContributions().add(contribution);
        }

        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            for (Long pId : request.participantIds()) {
                User participantUser = userRepository.findById(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Participante não encontrado: " + pId));

                if (!trip.hasMember(participantUser.getId().toString())) {
                    throw new IllegalArgumentException("O participante " + participantUser.getUsername() + " não é membro desta viagem.");
                }

                TripExpenseParticipant participant = TripExpenseParticipant.builder()
                    .expense(expense)
                    .participant(participantUser)
                    .build();
                expense.getParticipants().add(participant);
            }
        }

        TripExpense savedExpense = expenseRepository.save(expense);

        billingOrchestrator.settleExpense(savedExpense.getId().toString());

        TripExpense refreshedExpense = expenseRepository.findById(savedExpense.getId())
            .orElse(savedExpense);

        return TripExpenseResponse.from(refreshedExpense);
    }

    @Transactional(readOnly = true)
    public List<TripExpenseResponse> getTripExpenses(Long tripId, User loggedUser) {
        Group trip = groupRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada."));

        if (!trip.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta viagem.");
        }

        return expenseRepository.findByTripIdOrderByExpenseDateDesc(tripId).stream()
            .map(TripExpenseResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TripDebtResponse> getTripDebts(Long tripId, User loggedUser) {
        Group trip = groupRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada."));

        if (!trip.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta viagem.");
        }

        return debtRepository.findByExpenseTripId(tripId).stream()
            .map(TripDebtResponse::from)
            .toList();
    }

    @Transactional
    public TripDebtResponse updateDebtStatus(Long tripId, Long debtId, DebtStatus newStatus, User loggedUser) {
        TripDebt debt = debtRepository.findById(debtId)
            .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada."));

        if (!debt.getExpense().getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Dívida não pertence à viagem informada.");
        }

        if (newStatus == DebtStatus.AWAITING_CONFIRMATION) {
            if (!debt.getDebtor().getId().equals(loggedUser.getId())) {
                throw new IllegalArgumentException("Apenas o devedor pode informar o pagamento para confirmação.");
            }
        } else if (newStatus == DebtStatus.PAID) {
            if (!debt.getCreditor().getId().equals(loggedUser.getId())) {
                throw new IllegalArgumentException("Apenas o credor pode confirmar o recebimento do pagamento.");
            }
        } else if (newStatus == DebtStatus.CANCELLED) {
            if (!debt.getCreditor().getId().equals(loggedUser.getId()) && !debt.getDebtor().getId().equals(loggedUser.getId())) {
                throw new IllegalArgumentException("Apenas os envolvidos na dívida podem cancelá-la.");
            }
        }

        debt.setStatus(newStatus);
        TripDebt saved = debtRepository.save(debt);
        return TripDebtResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PixCodeResponse generatePixCodeForDebt(Long tripId, Long debtId, User loggedUser) {
        TripDebt debt = debtRepository.findById(debtId)
            .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada."));

        if (!debt.getExpense().getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Dívida não pertence à viagem informada.");
        }

        if (!debt.getExpense().getTrip().hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta viagem.");
        }

        User creditor = debt.getCreditor();
        if (creditor.getPixInfo() == null || creditor.getPixInfo().getPixKey() == null) {
            throw new IllegalArgumentException("O credor " + creditor.getUsername() + " não possui chave Pix configurada.");
        }

        String pixCode = pixCodeGenerator.generatePixCode(
            creditor.getPixInfo(),
            debt.getAmountInCents()
        );

        return new PixCodeResponse(
            pixCode,
            creditor.getPixInfo().getPixKey(),
            creditor.getPixInfo().getMerchantName(),
            creditor.getPixInfo().getMerchantCity(),
            debt.getAmountInCents()
        );
    }
}
