package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.group.CreateGroupRequest;
import com.role.net.tripmaker.dto.group.GroupResponse;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.TripGroupRepository;

import gogather.framework.group.jpa.domain.BaseUser;
import gogather.framework.group.jpa.service.GroupService;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import gogather.framework.group.web.controller.AbstractGroupController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController extends AbstractGroupController<Group, CreateGroupRequest> {

    private final TripGroupRepository groupRepository;

    public GroupController(
        GroupService frameworkGroupService,
        GroupMembershipOrchestrator orchestrator,
        TripGroupRepository groupRepository
    ) {
        super(frameworkGroupService, orchestrator);
        this.groupRepository = groupRepository;
    }

    @Override
    protected Group mapToEntity(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setDestination(request.destination());
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        if (request.maxTravelers() != null) {
            group.setMaxTravelers(request.maxTravelers());
        }
        return group;
    }

    @Override
    protected BaseUser getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(@AuthenticationPrincipal User user) {
        List<GroupResponse> response = groupRepository.findGroupsByUserId(user.getId())
            .stream()
            .map(GroupResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{inviteCode}")
    public ResponseEntity<GroupResponse> getGroupDetails(
        @PathVariable String inviteCode,
        @AuthenticationPrincipal User user
    ) {
        if (!groupRepository.isGroupMemberByInviteCode(inviteCode, user.getId())) {
            throw new ResourceNotFoundException("Viagem não encontrada ou você não é membro.");
        }
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada: " + inviteCode));
        return ResponseEntity.ok(GroupResponse.from(group));
    }
}