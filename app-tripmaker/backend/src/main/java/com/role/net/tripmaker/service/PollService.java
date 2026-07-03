package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.chat.ChatMessageResponse;
import com.role.net.tripmaker.dto.chat.PollRequest;
import com.role.net.tripmaker.dto.chat.PollResponse;
import com.role.net.tripmaker.entity.*;
import com.role.net.tripmaker.repository.ChatMessageRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.PollOptionRepository;
import com.role.net.tripmaker.repository.PollRepository;
import gogather.framework.polling.orchestrator.PollingOrchestrator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PollService {

    private final TripGroupRepository groupRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollingOrchestrator pollingOrchestrator;
    private final SimpMessagingTemplate messagingTemplate;

    public PollService(
            TripGroupRepository groupRepository,
            ChatMessageRepository chatMessageRepository,
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            PollingOrchestrator pollingOrchestrator,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.groupRepository = groupRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollingOrchestrator = pollingOrchestrator;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void createPoll(String inviteCode, PollRequest request, User user) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        ChatMessage message = ChatMessage.builder()
                .group(group)
                .sender(user)
                .content(request.question())
                .type(MessageType.POLL)
                .build();

        Poll poll = Poll.builder()
                .chatMessage(message)
                .build();

        List<PollOption> options = request.options().stream().map(optText -> PollOption.builder()
                .poll(poll)
                .text(optText)
                .votes(0)
                .build()).toList();

        poll.setOptions(options);
        message.setPoll(poll);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.from(savedMessage);
        messagingTemplate.convertAndSend("/topic/group/" + inviteCode, response);
    }

    @Transactional
    public void vote(Long optionId, String inviteCode, User user) {
        PollOption targetOption = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Opção de enquete não encontrada"));

        Poll poll = targetOption.getPoll();

        pollingOrchestrator.processVote(
                poll.getId().toString(),
                optionId.toString(),
                user.getId().toString()
        );

        Poll updatedPoll = pollRepository.findById(poll.getId()).orElseThrow();
        PollResponse response = PollResponse.from(updatedPoll);
        messagingTemplate.convertAndSend("/topic/group/" + inviteCode + "/poll-update", response);
    }
}
