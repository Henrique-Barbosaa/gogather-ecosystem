package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.itinerary.CreateItineraryRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.ItineraryEvent;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.ItineraryRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import gogather.framework.sequence.SequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripGroupRepository groupRepository;
    private final SequenceService sequenceService;

    public ItineraryService(ItineraryRepository itineraryRepository, TripGroupRepository groupRepository, SequenceService sequenceService) {
        this.itineraryRepository = itineraryRepository;
        this.groupRepository = groupRepository;
        this.sequenceService = sequenceService;
    }

    private Group validateMembership(String groupIdOrCode, User loggedUser) {
        Group group = null;
        try {
            Long id = Long.parseLong(groupIdOrCode);
            group = groupRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Not numeric
        }
        if (group == null) {
            group = groupRepository.findByInviteCode(groupIdOrCode)
                    .orElseThrow(() -> new IllegalArgumentException("Viagem não encontrada."));
        }
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new IllegalArgumentException("Você não é membro desta viagem.");
        }
        return group;
    }

    @Transactional
    public ItineraryEvent createEvent(String groupIdOrCode, CreateItineraryRequest request, User loggedUser) {
        Group group = validateMembership(groupIdOrCode, loggedUser);
        Long realGroupId = group.getId();
        List<ItineraryEvent> existingEvents = itineraryRepository.findByGroupIdOrderByStartTimeAsc(realGroupId);

        ItineraryEvent event = ItineraryEvent.builder()
                .group(group)
                .creator(loggedUser)
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime() != null ? request.startTime() : java.time.LocalDateTime.now())
                .endTime(request.endTime() != null ? request.endTime() : java.time.LocalDateTime.now().plusHours(2))
                .location(request.location() != null ? request.location() : "Local a definir")
                .costEstimateCents(request.costEstimateCents() != null ? request.costEstimateCents() : 0L)
                .day(request.day() != null ? request.day() : "Dia 1")
                .time(request.time() != null ? request.time() : "12:00")
                .category(request.category() != null ? request.category() : "Passeio")
                .build();

        sequenceService.appendItem(event, existingEvents);
        return itineraryRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<ItineraryEvent> getEvents(String groupIdOrCode, User loggedUser) {
        Group group = validateMembership(groupIdOrCode, loggedUser);
        List<ItineraryEvent> events = itineraryRepository.findByGroupIdOrderByStartTimeAsc(group.getId());
        return sequenceService.getOrderedItems(events);
    }

    @Transactional
    public void deleteEvent(String groupIdOrCode, Long eventId, User loggedUser) {
        Group group = validateMembership(groupIdOrCode, loggedUser);
        Long realGroupId = group.getId();
        List<ItineraryEvent> events = itineraryRepository.findByGroupIdOrderByStartTimeAsc(realGroupId);
        ItineraryEvent event = events.stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        if (!event.getGroup().getId().equals(realGroupId)) {
            throw new IllegalArgumentException("Evento não pertence a esta viagem.");
        }
        sequenceService.removeItem(event, events);
        itineraryRepository.delete(event);
        itineraryRepository.saveAll(events);
    }

    @Transactional
    public List<ItineraryEvent> reorderEvents(String groupIdOrCode, Long eventId, int newIndex, User loggedUser) {
        Group group = validateMembership(groupIdOrCode, loggedUser);
        Long realGroupId = group.getId();
        List<ItineraryEvent> events = itineraryRepository.findByGroupIdOrderByStartTimeAsc(realGroupId);
        sequenceService.normalizeSequence(events);
        ItineraryEvent targetEvent = events.stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        sequenceService.moveToIndex(targetEvent, newIndex, events);
        return itineraryRepository.saveAll(events);
    }
}
