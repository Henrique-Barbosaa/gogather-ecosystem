package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.itinerary.CreateItineraryRequest;
import com.role.net.tripmaker.dto.itinerary.ItineraryResponse;
import com.role.net.tripmaker.entity.ItineraryEvent;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.ItineraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{groupId}/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    public ResponseEntity<ItineraryResponse> createEvent(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateItineraryRequest request,
            @AuthenticationPrincipal User loggedUser) {
        ItineraryEvent saved = itineraryService.createEvent(groupId, request, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItineraryResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<ItineraryResponse>> getEvents(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        List<ItineraryResponse> responses = itineraryService.getEvents(groupId, loggedUser).stream()
                .map(ItineraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long groupId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User loggedUser) {
        itineraryService.deleteEvent(groupId, eventId, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
