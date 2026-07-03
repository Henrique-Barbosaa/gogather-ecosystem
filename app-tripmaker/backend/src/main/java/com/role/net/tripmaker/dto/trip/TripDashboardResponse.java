package com.role.net.tripmaker.dto.trip;

import com.role.net.tripmaker.dto.itinerary.ItineraryResponse;
import com.role.net.tripmaker.dto.packing.PackingItemResponse;
import java.util.List;

public record TripDashboardResponse(
    Long groupId,
    String groupName,
    String destination,
    Long myTotalDebtCents,
    Long totalOwedToMeCents,
    List<ItineraryResponse> upcomingEvents,
    List<PackingItemResponse> pendingPackingItems
) {}
