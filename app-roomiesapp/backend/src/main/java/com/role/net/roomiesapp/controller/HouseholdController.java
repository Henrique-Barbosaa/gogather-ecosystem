package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.household.DashboardResponse;
import com.role.net.roomiesapp.dto.household.UpdateHouseholdRequest;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{groupId}")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PutMapping
    public ResponseEntity<Void> updateHousehold(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateHouseholdRequest request,
            @AuthenticationPrincipal User loggedUser) {
        householdService.updateHousehold(groupId, request, loggedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        DashboardResponse response = householdService.getDashboard(groupId, loggedUser);
        return ResponseEntity.ok(response);
    }
}
