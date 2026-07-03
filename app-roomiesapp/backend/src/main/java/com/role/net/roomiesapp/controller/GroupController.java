package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.group.CreateGroupRequest;
import com.role.net.roomiesapp.dto.group.GroupResponse;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController extends AbstractGroupController<Group, CreateGroupRequest> {

    private final RoomiesGroupRepository groupRepository;

    public GroupController(
        GroupService frameworkGroupService,
        GroupMembershipOrchestrator orchestrator,
        RoomiesGroupRepository groupRepository
    ) {
        super(frameworkGroupService, orchestrator);
        this.groupRepository = groupRepository;
    }

    @Override
    protected Group mapToEntity(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setAddress(request.address());
        group.setMonthlyRentCents(request.monthlyRentCents());
        if (request.maxOccupants() != null) {
            group.setMaxOccupants(request.maxOccupants());
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
            throw new ResourceNotFoundException("Grupo não encontrado ou você não é membro.");
        }
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado: " + inviteCode));
        return ResponseEntity.ok(GroupResponse.from(group));
    }
}