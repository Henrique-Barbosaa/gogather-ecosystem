package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.chore.ChoreResponse;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.UserRepository;
import com.role.net.roomiesapp.service.ChoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households/{groupId}/chores")
public class ChoreController {

    private final ChoreService choreService;
    private final RoomiesGroupRepository groupRepository;
    private final UserRepository userRepository;

    public ChoreController(ChoreService choreService, RoomiesGroupRepository groupRepository, UserRepository userRepository) {
        this.choreService = choreService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    private void validateMembership(Long groupId, User loggedUser) {
        if (!groupRepository.findById(groupId).orElseThrow().hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }
    }

    @GetMapping
    public ResponseEntity<List<ChoreResponse>> getChores(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        validateMembership(groupId, loggedUser);
        List<ChoreResponse> responses = choreService.getChores(groupId).stream()
                .map(ChoreResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{choreId}/assign/{userId}")
    public ResponseEntity<ChoreResponse> assignChore(
            @PathVariable Long groupId,
            @PathVariable Long choreId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User loggedUser) {
        validateMembership(groupId, loggedUser);
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        
        // Ensure assignee is in the group
        validateMembership(groupId, assignee);

        ChoreResponse response = ChoreResponse.from(choreService.assignChore(choreId, assignee));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{choreId}/complete")
    public ResponseEntity<ChoreResponse> completeChore(
            @PathVariable Long groupId,
            @PathVariable Long choreId,
            @AuthenticationPrincipal User loggedUser) {
        validateMembership(groupId, loggedUser);
        ChoreResponse response = ChoreResponse.from(choreService.completeChore(choreId));
        return ResponseEntity.ok(response);
    }
}
