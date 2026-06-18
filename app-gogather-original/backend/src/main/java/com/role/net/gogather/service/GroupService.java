package com.role.net.gogather.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.dto.group.CreateGroupRequest;
import com.role.net.gogather.dto.group.GroupDetailsResponse;
import com.role.net.gogather.dto.group.GroupResponse;
import com.role.net.gogather.entity.EventStop;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.GroupMember;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.GroupMemberStatus;
import com.role.net.gogather.enums.GroupRole;
import com.role.net.gogather.exception.InvalidRequestException;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.exception.UserNotAGroupMemberException;
import com.role.net.gogather.repository.GroupRepository;
import com.role.net.gogather.repository.UserRepository;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PlacesApiService placesApiService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, PlacesApiService placesApiService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.placesApiService = placesApiService;
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request, Long userId) {
        User adminUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Group group = Group.builder()
			.name(request.name())
			.description(request.description())
			.eventDate(request.date())
			.build();

        Group savedGroup = groupRepository.save(group);

		request.stops().forEach(stopRequest -> {
            EventStop stop = EventStop.builder()
				.name(stopRequest.name())
				.latitude(stopRequest.latitude())
				.longitude(stopRequest.longitude())
				.category(stopRequest.category())
				.stopOrder(stopRequest.order())
				.city(stopRequest.city())
				.state(stopRequest.state())
				.group(savedGroup)
				.build();

            savedGroup.getEventStops().add(stop);
        });

        GroupMember adminMember = GroupMember.builder()
			.group(savedGroup)
			.user(adminUser)
			.role(GroupRole.ADMIN)
            .status(com.role.net.gogather.enums.GroupMemberStatus.ACTIVE)
            .invitedBy(null)
			.build();

        savedGroup.getMembers().add(adminMember);

        Group updatedGroup = groupRepository.save(savedGroup);

        return new GroupResponse(
			updatedGroup.getExternalId(),
			updatedGroup.getName(),
			updatedGroup.getDescription(),
			updatedGroup.getInviteCode(),
			updatedGroup.getEventDate(),
			updatedGroup.getMembers().size()
        );
    }

	public List<GroupResponse> getUserGroups(Long userId) {
        return groupRepository.findGroupsByUserId(userId, GroupMemberStatus.ACTIVE).stream()
			.map(group -> new GroupResponse(
				group.getExternalId(),
				group.getName(),
				group.getDescription(),
				group.getInviteCode(),
				group.getEventDate(),
				group.getMembers().size()
			))
			.toList();
    }

	@Transactional(readOnly = true)
	public GroupDetailsResponse getGroupDetails(UUID externalId, Long userId) {
        Group group = groupRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

		System.out.println("DEBUG: Checking if user " + userId + " is member of group " + externalId.toString());
        boolean isMember = groupRepository.isGroupMemberByExternalId(externalId, userId, GroupMemberStatus.ACTIVE);
		System.out.println("DEBUG: User " + userId + " is member of group " + externalId.toString() + ": " + isMember);

        if (!isMember) {
            throw new UserNotAGroupMemberException("User is not a member of this group");
        }

        List<GroupDetailsResponse.MemberDTO> members = group.getMembers().stream()
			.map(member -> new GroupDetailsResponse.MemberDTO(
				member.getUser().getExternalId(),
				member.getUser().getUsername(),
				member.getUser().getDisplayName(),
				member.getRole(),
				member.getUser().getEmail()
			))
			.toList();

        List<GroupDetailsResponse.EventStopDTO> eventStops = group.getEventStops().stream()
			.map(stop -> new GroupDetailsResponse.EventStopDTO(
				stop.getName(),
				stop.getLatitude(),
				stop.getLongitude(),
				stop.getCategory(),
				stop.getStopOrder(),
				stop.getCity(),
				stop.getState(),
                stop.getPlaceId()
			))
			.toList();

        return new GroupDetailsResponse(
			group.getExternalId(),
			group.getName(),
			group.getDescription(),
			group.getInviteCode(),
			group.getCreatedAt(),
			group.getEventDate(),
			members,
			eventStops
        );
    }

	@Transactional
    public void joinGroupByInviteCode(String inviteCode, User loggedInUser) {
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Rolê não encontrado com esse código."));

        boolean alreadyMember = group.getMembers().stream()
            .anyMatch(member -> member.getUser().getId().equals(loggedInUser.getId()));

        if (alreadyMember) {
            throw new InvalidRequestException("Você já faz parte deste rolê!");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroup(group);
        newMember.setUser(loggedInUser);
        newMember.setStatus(GroupMemberStatus.ACTIVE);
        newMember.setInvitedBy(null);

        newMember.setRole(GroupRole.MEMBER);

        group.getMembers().add(newMember);
        groupRepository.save(group);
    }

    @Transactional
    public void inviteFriendToGroup(UUID groupId, UUID friendId, User loggedInUser) {
        Group group = groupRepository.findByExternalId(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Rolê não encontrado."));

        User friend = userRepository.findByExternalId(friendId)
            .orElseThrow(() -> new ResourceNotFoundException("Amigo não encontrado."));

        // AFAZER verificar aqui no FriendshipRepository se eles realmente são amigos

        boolean alreadyMember = group.getMembers().stream()
            .anyMatch(member -> member.getUser().getExternalId().equals(friendId));

        if (alreadyMember) {
            throw new InvalidRequestException("Esse usuário já está no rolê ou já foi convidado.");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroup(group);
        newMember.setUser(friend);
        newMember.setStatus(GroupMemberStatus.PENDING);
        newMember.setInvitedBy(loggedInUser);

        newMember.setRole(GroupRole.MEMBER);

        group.getMembers().add(newMember);
        groupRepository.save(group);
    }

    @Transactional
    public void acceptGroupInvite(UUID groupId, User loggedInUser) {
        Group group = groupRepository.findByExternalId(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Rolê não encontrado."));

        GroupMember invite = group.getMembers().stream()
            .filter(member -> member.getUser().getId().equals(loggedInUser.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado."));

        if (invite.getStatus() == GroupMemberStatus.ACTIVE) {
            throw new InvalidRequestException("Você já faz parte deste rolê.");
        }
        invite.setStatus(GroupMemberStatus.ACTIVE);

        groupRepository.save(group);
    }

    @Transactional
    public void addEventStopFromPlace(UUID groupId, String placeId, User adminUser) {
        Group group = groupRepository.findByExternalId(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        GroupMember adminMember = group.getMembers().stream()
            .filter(member -> member.getUser().getId().equals(adminUser.getId()))
            .findFirst()
            .orElseThrow(() -> new UserNotAGroupMemberException("User is not a member of this group"));

        if (adminMember.getRole() != GroupRole.ADMIN) {
            throw new InvalidRequestException("Apenas administradores podem adicionar paradas ao roteiro.");
        }

        JsonNode placeDetails = placesApiService.getPlaceDetails(placeId);

        String name = placeDetails.path("displayName").path("text").asText();
        double latitude = placeDetails.path("location").path("latitude").asDouble();
        double longitude = placeDetails.path("location").path("longitude").asDouble();

        String city = null;
        String state = null;

        JsonNode addressComponents = placeDetails.path("addressComponents");
        if (addressComponents.isArray()) {
            for (JsonNode comp : addressComponents) {
                JsonNode types = comp.path("types");
                if (types.isArray()) {
                    boolean isCity = false;
                    boolean isState = false;
                    for (JsonNode type : types) {
                        if ("administrative_area_level_2".equals(type.asText()) || "locality".equals(type.asText())) {
                            isCity = true;
                        }
                        if ("administrative_area_level_1".equals(type.asText())) {
                            isState = true;
                        }
                    }
                    if (isCity && city == null) city = comp.path("longText").asText();
                    if (isState && state == null) state = comp.path("shortText").asText();
                }
            }
        }

        int nextOrder = group.getEventStops().size() + 1;

        EventStop stop = EventStop.builder()
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .category("Recomendação da IA")
            .stopOrder(nextOrder)
            .city(city)
            .state(state)
            .placeId(placeId)
            .group(group)
            .build();

        group.getEventStops().add(stop);
        groupRepository.save(group);
    }
}
