package com.role.net.roomiesapp.service.provider;

import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.HouseBill;
import com.role.net.roomiesapp.entity.HouseDebt;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.HouseBillRepository;
import com.role.net.roomiesapp.repository.HouseDebtRepository;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.UserRepository;
import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.core.Participant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoomiesBillingDataProvider implements BillingDataProvider {

    public static final String HOUSE_UNASSIGNED_IDENTIFIER = "HOUSE_UNASSIGNED";

    private final HouseBillRepository billRepository;
    private final HouseDebtRepository debtRepository;
    private final RoomiesGroupRepository groupRepository;
    private final UserRepository userRepository;

    public RoomiesBillingDataProvider(
            HouseBillRepository billRepository,
            HouseDebtRepository debtRepository,
            RoomiesGroupRepository groupRepository,
            UserRepository userRepository) {
        this.billRepository = billRepository;
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCents(String expenseId) {
        HouseBill bill = findBill(expenseId);
        return bill.getTotalCents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String expenseId) {
        HouseBill bill = findBill(expenseId);
        if (bill.getParticipants() != null && !bill.getParticipants().isEmpty()) {
            return new ArrayList<>(bill.getParticipants());
        }
        Group group = bill.getGroup();
        return group.getMembers().stream()
                .map(m -> (Participant) m.getUser())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contribution> getContributions(String expenseId) {
        HouseBill bill = findBill(expenseId);
        if (bill.getContributor() != null) {
            return List.of(new Contribution(bill.getContributor(), bill.getTotalCents()));
        }
        // Para contas recorrentes que não possuem pagador/contribuidor definido de início,
        // geramos um participante sintético representando a casa para que o algoritmo do framework divida a conta.
        Participant syntheticHouseCreditor = () -> HOUSE_UNASSIGNED_IDENTIFIER;
        return List.of(new Contribution(syntheticHouseCreditor, bill.getTotalCents()));
    }

    @Override
    @Transactional
    public void saveDistributions(String expenseId, List<DebtDistribution> distributions) {
        HouseBill bill = findBill(expenseId);
        debtRepository.deleteByBill(bill);

        for (DebtDistribution dist : distributions) {
            User debtor = findUserByIdentifier(dist.debtor().getIdentifier());
            User creditor = null;
            if (!HOUSE_UNASSIGNED_IDENTIFIER.equals(dist.creditor().getIdentifier())) {
                creditor = findUserByIdentifier(dist.creditor().getIdentifier());
            }

            HouseDebt debt = new HouseDebt();
            debt.setBill(bill);
            debt.setDebtor(debtor);
            debt.setCreditor(creditor);
            debt.setAmountInCents(dist.amountInCents());
            debt.setStatus(dist.status() != null ? dist.status() : DebtStatus.PENDING);

            debtRepository.save(debt);
        }
    }

    private HouseBill findBill(String expenseId) {
        UUID extId;
        try {
            extId = UUID.fromString(expenseId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID da despesa inválido: " + expenseId);
        }
        return billRepository.findByExternalId(extId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + expenseId));
    }

    private User findUserByIdentifier(String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier);
            return userRepository.findByExternalId(uuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado por externalId: " + identifier));
        } catch (IllegalArgumentException e) {
            try {
                Long id = Long.valueOf(identifier);
                return userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado por id: " + identifier));
            } catch (NumberFormatException ex) {
                return userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + identifier));
            }
        }
    }
}
