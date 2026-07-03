package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.packing.CreatePackingItemRequest;
import com.role.net.tripmaker.dto.packing.PackingItemResponse;
import com.role.net.tripmaker.entity.PackingItem;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.PackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{groupId}/packing")
public class PackingController {

    private final PackingService packingService;

    public PackingController(PackingService packingService) {
        this.packingService = packingService;
    }

    @PostMapping
    public ResponseEntity<PackingItemResponse> addItem(
            @PathVariable Long groupId,
            @Valid @RequestBody CreatePackingItemRequest request,
            @AuthenticationPrincipal User loggedUser) {
        PackingItem saved = packingService.addItem(groupId, request, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(PackingItemResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<PackingItemResponse>> getList(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        List<PackingItemResponse> responses = packingService.getList(groupId, loggedUser).stream()
                .map(PackingItemResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{itemId}/assign/{userId}")
    public ResponseEntity<PackingItemResponse> assignItem(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User loggedUser) {
        PackingItem saved = packingService.assignItem(groupId, itemId, userId, loggedUser);
        return ResponseEntity.ok(PackingItemResponse.from(saved));
    }

    @PutMapping("/{itemId}/pack")
    public ResponseEntity<PackingItemResponse> markAsPacked(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User loggedUser) {
        PackingItem saved = packingService.markAsPacked(groupId, itemId, loggedUser);
        return ResponseEntity.ok(PackingItemResponse.from(saved));
    }
}
