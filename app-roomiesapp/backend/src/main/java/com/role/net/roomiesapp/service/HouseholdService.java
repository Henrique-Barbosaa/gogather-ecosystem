package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.chore.ChoreResponse;
import com.role.net.roomiesapp.dto.household.DashboardResponse;
import com.role.net.roomiesapp.dto.household.UpdateHouseholdRequest;
import com.role.net.roomiesapp.dto.shopping.ShoppingItemResponse;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import gogather.framework.billing.dto.DebtStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HouseholdService {

    private final RoomiesGroupRepository groupRepository;
    private final ChoreService choreService;
    private final ShoppingService shoppingService;
    private final RoomiesBillingService billingService;

    public HouseholdService(
            RoomiesGroupRepository groupRepository,
            ChoreService choreService,
            ShoppingService shoppingService,
            RoomiesBillingService billingService) {
        this.groupRepository = groupRepository;
        this.choreService = choreService;
        this.shoppingService = shoppingService;
        this.billingService = billingService;
    }

    private Group validateMembership(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada."));
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }
        return group;
    }

    @Transactional
    public void updateHousehold(Long groupId, UpdateHouseholdRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);
        group.setAddress(request.address());
        group.setMonthlyRentCents(request.monthlyRentCents());
        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long groupId, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        List<ChoreResponse> myPendingChores = choreService.getChores(groupId).stream()
                .filter(c -> !c.isCompleted() && c.getAssignee() != null && c.getAssignee().getId().equals(loggedUser.getId()))
                .map(ChoreResponse::from)
                .toList();

        List<ShoppingItemResponse> pendingShopping = shoppingService.getList(groupId, loggedUser).stream()
                .filter(s -> !s.bought())
                .toList();

        long myTotalDebtCents = billingService.getMyDebts(loggedUser).stream()
                .filter(d -> d.status() != DebtStatus.PAID && d.status() != DebtStatus.CANCELLED)
                .mapToLong(d -> d.amountInCents())
                .sum();

        long totalOwedToMeCents = billingService.getMyCredits(loggedUser).stream()
                .filter(d -> d.status() != DebtStatus.PAID && d.status() != DebtStatus.CANCELLED)
                .mapToLong(d -> d.amountInCents())
                .sum();

        return new DashboardResponse(
                group.getId(),
                group.getName(),
                myTotalDebtCents,
                totalOwedToMeCents,
                myPendingChores,
                pendingShopping
        );
    }
}
