package com.role.net.gogather.controller;

import com.role.net.gogather.entity.Expense;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.ExpenseService;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import gogather.framework.group.web.controller.AbstractGroupController;
import gogather.framework.group.jpa.domain.BaseUser;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
public class GroupController extends AbstractGroupController<Group, CreateGroupRequest> {

    private final com.role.net.gogather.service.GroupService appGroupService;
    private final ExpenseService expenseService;

    public GroupController(
        gogather.framework.group.jpa.service.GroupService frameworkGroupService,
        GroupMembershipOrchestrator orchestrator,
        com.role.net.gogather.service.GroupService appGroupService,
        ExpenseService expenseService
    ) {
        super(frameworkGroupService, orchestrator);
        this.appGroupService = appGroupService;
        this.expenseService = expenseService;
    }

    @Override
    protected Group mapToEntity(CreateGroupRequest request) {
        Group group = new Group();
        
        group.setName(request.name());               
        group.setDescription(request.description()); 
        group.setEventDate(request.date());          
        
        if (request.stops() != null && !request.stops().isEmpty()) {
            
            List<EventStop> paradas = request.stops().stream().map(stopReq -> {
                EventStop stop = new EventStop();
                stop.setName(stopReq.name());
                stop.setLatitude(stopReq.latitude());
                stop.setLongitude(stopReq.longitude());
                stop.setCategory(stopReq.category());
                stop.setStopOrder(stopReq.order()); 
                stop.setCity(stopReq.city());
                stop.setState(stopReq.state());
                
                stop.setGroup(group); 
                
                return stop;
            }).toList();
            
            group.setEventStops(paradas);
        }
        
        return group;
    }

    @Override
    protected BaseUser getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(@AuthenticationPrincipal User user) {
        List<GroupResponse> response = appGroupService.getUserGroups(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{inviteCode}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal User user
    ) {
        GroupDetailsResponse response = appGroupService.getGroupDetails(inviteCode, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{inviteCode}/invite/{friendId}")
    public ResponseEntity<Void> inviteFriend(
            @PathVariable String inviteCode,
            @PathVariable Long friendId,
            @AuthenticationPrincipal User loggedInUser
    ) {
        orchestrator.inviteUserToGroup(inviteCode, friendId.toString(), loggedInUser.getId().toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inviteCode}/expense/auto")
    public ResponseEntity<ExpenseResponse> createExpenseAuto(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode,
        @RequestBody ExpenseAutoCreationRequest request
    ) {
        Expense expense = expenseService.createAuto(user, inviteCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.from(expense));
    }

    @PostMapping("/{inviteCode}/expense/manual")
    public ResponseEntity<ExpenseResponse> createExpenseManual(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode,
        @RequestBody ExpenseManualCreationRequest request
    ) {
        Expense expense = expenseService.createManual(user, inviteCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.from(expense));
    }

    @PostMapping("/{inviteCode}/stops/from-place/{placeId}")
    public ResponseEntity<Void> addStopFromPlace(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode,
        @PathVariable String placeId
    ) {
        appGroupService.addEventStopFromPlace(inviteCode, placeId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{inviteCode}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode
    ) {
        appGroupService.getGroupDetails(inviteCode, user.getId());
        List<ExpenseResponse> expenses = expenseService.getGroupExpenses(inviteCode, user.getId());
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{inviteCode}/stops/reorder")
    public ResponseEntity<Void> reorderStops(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode,
        @Valid @RequestBody ReorderStopsRequest request
    ) {
        appGroupService.reorderStops(inviteCode, request.stopIdsInOrder(), user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inviteCode}/stops/delete-batch")
    public ResponseEntity<Void> removeStopsBatch(
        @AuthenticationPrincipal User user,
        @PathVariable String inviteCode,
        @Valid @RequestBody RemoveStopsRequest request
    ) {
        appGroupService.removeStopsBatch(inviteCode, request.stopIdsToRemove(), user);
        return ResponseEntity.ok().build();
    }
}