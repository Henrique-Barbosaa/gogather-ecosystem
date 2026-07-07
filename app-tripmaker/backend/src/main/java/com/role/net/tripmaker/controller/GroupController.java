package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.group.AddGroupMemberRequest;
import com.role.net.tripmaker.dto.group.CreateGroupRequest;
import com.role.net.tripmaker.dto.group.GroupResponse;
import com.role.net.tripmaker.dto.group.MemberResponse;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.ChatMessageRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;

import gogather.framework.group.jpa.domain.BaseUser;
import gogather.framework.group.jpa.service.GroupService;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import gogather.framework.group.web.controller.AbstractGroupController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
public class GroupController extends AbstractGroupController<Group, CreateGroupRequest> {

    private final TripGroupRepository groupRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public GroupController(
        GroupService frameworkGroupService,
        GroupMembershipOrchestrator orchestrator,
        TripGroupRepository groupRepository,
        ChatMessageRepository chatMessageRepository,
        UserRepository userRepository
    ) {
        super(frameworkGroupService, orchestrator);
        this.groupRepository = groupRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected Group mapToEntity(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setDestination(request.destination());
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        if (request.coverUrl() != null && !request.coverUrl().isBlank()) {
            group.setCoverUrl(request.coverUrl());
        } else {
            group.setCoverUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80");
        }
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
    @Transactional(readOnly = true)
    public ResponseEntity<List<GroupResponse>> getUserGroups(@AuthenticationPrincipal User user) {
        List<GroupResponse> response = groupRepository.findGroupsByUserId(user.getId())
            .stream()
            .map(g -> GroupResponse.from(g, chatMessageRepository.findByGroupIdOrderByCreatedAtAsc(g.getId())))
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idOrCode}")
    @Transactional(readOnly = true)
    public ResponseEntity<GroupResponse> getGroupDetails(
        @PathVariable String idOrCode,
        @AuthenticationPrincipal User user
    ) {
        Group group = null;
        boolean isMember = false;
        try {
            Long id = Long.parseLong(idOrCode);
            group = groupRepository.findById(id).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMember(id, user.getId());
            }
        } catch (NumberFormatException e) {
            // Not numeric, lookup by invite code
        }
        if (group == null) {
            group = groupRepository.findByInviteCode(idOrCode).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMemberByInviteCode(idOrCode, user.getId());
            }
        }
        if (group == null || !isMember) {
            throw new ResourceNotFoundException("Viagem não encontrada ou você não é membro.");
        }
        return ResponseEntity.ok(GroupResponse.from(group, chatMessageRepository.findByGroupIdOrderByCreatedAtAsc(group.getId())));
    }

    @PostMapping({"/{idOrCode}/members", "/{idOrCode}/invite"})
    public ResponseEntity<MemberResponse> addGroupMember(
        @PathVariable String idOrCode,
        @RequestBody AddGroupMemberRequest request,
        @AuthenticationPrincipal User user
    ) {
        Group group = null;
        boolean isMember = false;
        try {
            Long id = Long.parseLong(idOrCode);
            group = groupRepository.findById(id).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMember(id, user.getId());
            }
        } catch (NumberFormatException e) {
        }
        if (group == null) {
            group = groupRepository.findByInviteCode(idOrCode).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMemberByInviteCode(idOrCode, user.getId());
            }
        }
        if (group == null || !isMember) {
            throw new ResourceNotFoundException("Viagem não encontrada ou você não tem permissão para adicionar membros.");
        }

        User invitee = null;
        if (request.userId() != null) {
            invitee = userRepository.findById(request.userId()).orElse(null);
        }
        if (invitee == null && request.email() != null && !request.email().isBlank()) {
            String identifier = request.email().trim();
            invitee = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElse(null);
        }
        if (invitee == null) {
            throw new ResourceNotFoundException("Usuário não encontrado no sistema com os dados fornecidos.");
        }

        if (groupRepository.isGroupMember(group.getId(), invitee.getId())) {
            throw new IllegalArgumentException("Este usuário já faz parte desta viagem!");
        }

        orchestrator.inviteUserToGroup(group.getInviteCode(), invitee.getId().toString(), user.getId().toString());

        Group updatedGroup = groupRepository.findById(group.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado após adição."));
        
        final User targetInvitee = invitee;
        gogather.framework.group.jpa.domain.GroupMember addedMember = updatedGroup.getMembers().stream()
            .filter(m -> m.getUser().getId().equals(targetInvitee.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Erro ao carregar o membro adicionado."));

        return ResponseEntity.ok(MemberResponse.from(addedMember));
    }
}