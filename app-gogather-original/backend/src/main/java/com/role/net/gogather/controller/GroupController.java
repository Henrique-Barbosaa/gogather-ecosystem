package com.role.net.gogather.controller;

import com.role.net.gogather.entity.Expense;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.ExpenseService;
import com.role.net.gogather.service.GroupService;
import com.role.net.gogather.dto.expense.ExpenseAutoCreationRequest;

import com.role.net.gogather.dto.expense.ExpenseManualCreationRequest;
import com.role.net.gogather.dto.expense.ExpenseResponse;
import com.role.net.gogather.dto.group.CreateGroupRequest;
import com.role.net.gogather.dto.group.GroupDetailsResponse;
import com.role.net.gogather.dto.group.GroupResponse;
import com.role.net.gogather.dto.group.RemoveStopsRequest;
import com.role.net.gogather.dto.group.ReorderStopsRequest;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;

    public GroupController(
        GroupService groupService,
        ExpenseService expenseService
    ) {
        this.groupService = groupService;
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal User user
	) {

        GroupResponse response = groupService.create(request, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

	@GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(@AuthenticationPrincipal User user) {
        List<GroupResponse> response = groupService.getUserGroups(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(
            @PathVariable UUID externalId,
            @AuthenticationPrincipal User user
    ) {
        GroupDetailsResponse response = groupService.getGroupDetails(externalId, user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<Void> joinGroup(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal User loggedInUser
    ) {
        groupService.joinGroupByInviteCode(inviteCode, loggedInUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/invite/{friendId}")
    public ResponseEntity<Void> inviteFriend(
            @PathVariable UUID groupId,
            @PathVariable UUID friendId,
            @AuthenticationPrincipal User loggedInUser
    ) {
        groupService.inviteFriendToGroup(groupId, friendId, loggedInUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}/accept")
    public ResponseEntity<Void> acceptInvite(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User loggedInUser
    ) {
        groupService.acceptGroupInvite(groupId, loggedInUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/expense/auto")
    public ResponseEntity<ExpenseResponse> createExpenseAuto(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId,
        @RequestBody ExpenseAutoCreationRequest request
    ) {
        Expense expense = expenseService.createAuto(user, groupId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ExpenseResponse.from(expense));
    }

    @PostMapping("/{groupId}/expense/manual")
    public ResponseEntity<ExpenseResponse> createExpenseManual(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId,
        @RequestBody ExpenseManualCreationRequest request
    ) {
        Expense expense = expenseService.createManual(user, groupId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ExpenseResponse.from(expense));
	}

    @PostMapping("/{groupId}/stops/from-place/{placeId}")
    public ResponseEntity<Void> addStopFromPlace(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId,
        @PathVariable String placeId
    ) {
        groupService.addEventStopFromPlace(groupId, placeId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId
    ) {
        // Optionally verify if user is part of the group first.
        // groupService.getGroupDetails will throw if not a member.
        groupService.getGroupDetails(groupId, user.getId());

        List<ExpenseResponse> expenses = expenseService.getGroupExpenses(groupId, user.getId());
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{groupId}/stops/reorder")
    public ResponseEntity<Void> reorderStops(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId,
        @Valid @RequestBody ReorderStopsRequest request
    ) {
        groupService.reorderStops(groupId, request.stopIdsInOrder(), user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/stops/delete-batch")
    public ResponseEntity<Void> removeStopsBatch(
        @AuthenticationPrincipal User user,
        @PathVariable UUID groupId,
        @Valid @RequestBody RemoveStopsRequest request
    ) {
        groupService.removeStopsBatch(groupId, request.stopIdsToRemove(), user);
        return ResponseEntity.ok().build();
    }
}
