package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.itinerary.CreateItineraryRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.ItineraryEvent;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.ItineraryRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripGroupRepository groupRepository;

    public ItineraryService(ItineraryRepository itineraryRepository, TripGroupRepository groupRepository) {
        this.itineraryRepository = itineraryRepository;
        this.groupRepository = groupRepository;
    }

    private Group validateMembership(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Viagem não encontrada."));
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new IllegalArgumentException("Você não é membro desta viagem.");
        }
        return group;
    }

    @Transactional
    public ItineraryEvent createEvent(Long groupId, CreateItineraryRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        ItineraryEvent event = ItineraryEvent.builder()
                .group(group)
                .creator(loggedUser)
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location())
                .costEstimateCents(request.costEstimateCents())
                .build();

        return itineraryRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<ItineraryEvent> getEvents(Long groupId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        return itineraryRepository.findByGroupIdOrderByStartTimeAsc(groupId);
    }

    @Transactional
    public void deleteEvent(Long groupId, Long eventId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        ItineraryEvent event = itineraryRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        
        if (!event.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Evento não pertence a esta viagem.");
        }
        itineraryRepository.delete(event);
    }
}
