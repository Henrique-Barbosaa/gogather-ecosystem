package com.role.net.gogather.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gogather.framework.sequence.SequenceService;
import gogather.framework.group.jpa.domain.GroupRole;

import com.role.net.gogather.dto.group.GroupDetailsResponse;
import com.role.net.gogather.dto.group.GroupResponse;
import com.role.net.gogather.entity.EventStop;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
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
    private final SequenceService sequenceService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, PlacesApiService placesApiService, SequenceService sequenceService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.placesApiService = placesApiService;
        this.sequenceService = sequenceService;
    }

    public List<GroupResponse> getUserGroups(Long userId) {
        return groupRepository.findGroupsByUserId(userId).stream()
            .map(group -> new GroupResponse(
                group.getInviteCode(),
                group.getName(),
                group.getDescription(),
                group.getInviteCode(),
                group.getEventDate(),
                group.getMembers().size()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailsResponse getGroupDetails(String inviteCode, Long userId) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        boolean isMember = groupRepository.isGroupMemberByInviteCode(inviteCode, userId);

        if (!isMember) {
            throw new UserNotAGroupMemberException("User is not a member of this group");
        }

        List<GroupDetailsResponse.MemberDTO> members = group.getMembers().stream()
            .map(member -> new GroupDetailsResponse.MemberDTO(
                member.getUser().getId().toString(),     // ID Universal
                ((User) member.getUser()).getUsername(), // Cast para a classe User do GoGather
                member.getUser().getName(),              // Nome fornecido pelo BaseUser
                member.getRole().name(),                 // Retorna a role do Framework (ADMIN/MEMBER)
                member.getUser().getEmail()              // Email do BaseUser
            ))
            .toList();

        List<GroupDetailsResponse.EventStopDTO> eventStops = group.getEventStops().stream()
            .map(stop -> new GroupDetailsResponse.EventStopDTO(
                stop.getId() != null ? stop.getId().toString() : "",
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
            group.getInviteCode(),
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
    public void addEventStopFromPlace(String inviteCode, String placeId, User adminUser) {
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        boolean isAdmin = group.getMembers().stream()
            .anyMatch(member -> member.getUser().getId().equals(adminUser.getId()) 
                             && member.getRole() == GroupRole.ADMIN);

        if (!isAdmin) {
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

        EventStop stop = EventStop.builder()
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .category("Recomendação da IA")
            .city(city)
            .state(state)
            .placeId(placeId)
            .group(group)
            .build();

        sequenceService.appendItem(stop, group.getEventStops());
        groupRepository.save(group);
    }

    @Transactional
    public void reorderStops(String inviteCode, List<Long> newOrderOfIds, User user) {
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Rolê não encontrado."));

        boolean isMember = groupRepository.isGroupMemberByInviteCode(inviteCode, user.getId());
        
        if (!isMember) {
            throw new UserNotAGroupMemberException("Usuário não faz parte do rolê.");
        }

        List<EventStop> stops = group.getEventStops();

        for (int i = 0; i < newOrderOfIds.size(); i++) {
            Long targetId = newOrderOfIds.get(i);
            stops.stream()
                 .filter(stop -> stop.getId().equals(targetId))
                 .findFirst()
                 .ifPresent(stop -> stop.setSequenceOrder(newOrderOfIds.indexOf(targetId))); 
        }

        sequenceService.normalizeSequence(stops);
        groupRepository.save(group);
    }

    @Transactional
    public void removeStopsBatch(String inviteCode, List<Long> stopIdsToRemove, User user) {
        Group group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Rolê não encontrado."));

        boolean isMember = groupRepository.isGroupMemberByInviteCode(inviteCode, user.getId());
        
        if (!isMember) {
            throw new UserNotAGroupMemberException("Usuário não faz parte do rolê.");
        }

        List<EventStop> stops = group.getEventStops();
        stops.removeIf(stop -> stopIdsToRemove.contains(stop.getId()));

        sequenceService.normalizeSequence(stops);
        groupRepository.save(group);
    }
}