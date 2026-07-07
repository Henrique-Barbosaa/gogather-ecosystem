package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.itinerary.ItineraryResponse;
import com.role.net.tripmaker.dto.packing.PackingItemResponse;
import com.role.net.tripmaker.dto.trip.TripDashboardResponse;
import com.role.net.tripmaker.dto.trip.UpdateTripRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.service.billing.TripBillingService;
import gogather.framework.billing.dto.DebtStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    private final TripGroupRepository groupRepository;
    private final ItineraryService itineraryService;
    private final PackingService packingService;
    private final TripBillingService tripBillingService;

    public TripService(
            TripGroupRepository groupRepository,
            ItineraryService itineraryService,
            PackingService packingService,
            TripBillingService tripBillingService) {
        this.groupRepository = groupRepository;
        this.itineraryService = itineraryService;
        this.packingService = packingService;
        this.tripBillingService = tripBillingService;
    }

    private Group validateMembership(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Viagem não encontrada."));
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new IllegalArgumentException("Você não é membro desta viagem.");
        }
        return group;
    }

    @Transactional
    public void updateTrip(Long groupId, UpdateTripRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);
        group.setDestination(request.destination());
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public TripDashboardResponse getDashboard(Long groupId, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        // Upcoming Itinerary Events
        List<ItineraryResponse> upcomingEvents = itineraryService.getEvents(String.valueOf(groupId), loggedUser).stream()
                .filter(e -> e.getStartTime() == null || e.getStartTime().isAfter(LocalDateTime.now().minusHours(1))) // show from 1h ago onwards
                .map(ItineraryResponse::from)
                .toList();

        // Pending Packing Items for the user
        List<PackingItemResponse> pendingPacking = packingService.getList(groupId, loggedUser).stream()
                .filter(p -> !p.isPacked() && p.getAssignee() != null && p.getAssignee().getId().equals(loggedUser.getId()))
                .map(PackingItemResponse::from)
                .toList();

        // Debts calculations
        var debts = tripBillingService.getTripDebts(groupId, loggedUser);

        long myTotalDebtCents = debts.stream()
                .filter(d -> d.debtorUsername().equals(loggedUser.getUsername())) // assuming we match by username
                .filter(d -> d.status() != DebtStatus.PAID && d.status() != DebtStatus.CANCELLED)
                .mapToLong(d -> d.amountInCents())
                .sum();

        long totalOwedToMeCents = debts.stream()
                .filter(d -> d.creditorUsername().equals(loggedUser.getUsername()))
                .filter(d -> d.status() != DebtStatus.PAID && d.status() != DebtStatus.CANCELLED)
                .mapToLong(d -> d.amountInCents())
                .sum();

        return new TripDashboardResponse(
                group.getId(),
                group.getName(),
                group.getDestination(),
                myTotalDebtCents,
                totalOwedToMeCents,
                upcomingEvents,
                pendingPacking
        );
    }
}
