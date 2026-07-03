package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.trip.TripDashboardResponse;
import com.role.net.tripmaker.dto.trip.UpdateTripRequest;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{groupId}")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PutMapping
    public ResponseEntity<Void> updateTrip(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateTripRequest request,
            @AuthenticationPrincipal User loggedUser) {
        tripService.updateTrip(groupId, request, loggedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<TripDashboardResponse> getDashboard(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        TripDashboardResponse response = tripService.getDashboard(groupId, loggedUser);
        return ResponseEntity.ok(response);
    }
}
