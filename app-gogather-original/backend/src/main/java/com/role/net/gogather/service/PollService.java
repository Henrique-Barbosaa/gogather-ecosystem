package com.role.net.gogather.service;

import com.role.net.gogather.dto.chat.PollResponse;
import com.role.net.gogather.entity.PollOption;
import com.role.net.gogather.entity.Poll;
import com.role.net.gogather.entity.PollVote;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.PollOptionRepository;
import com.role.net.gogather.repository.PollRepository;
import com.role.net.gogather.repository.PollVoteRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gogather.framework.polling.orchestrator.PollingOrchestrator;

import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class PollService {

    private final PollingOrchestrator pollingOrchestrator;
    private final PollOptionRepository pollOptionRepository;
    private final PollRepository pollRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public PollService(PollingOrchestrator pollingOrchestrator, 
                       PollOptionRepository pollOptionRepository, 
                       PollRepository pollRepository, 
                       SimpMessagingTemplate messagingTemplate) {
        this.pollingOrchestrator = pollingOrchestrator;
        this.pollOptionRepository = pollOptionRepository;
        this.pollRepository = pollRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void vote(Long optionId, UUID groupExternalId, User user) {
        PollOption targetOption = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opção de enquete não encontrada"));

        Poll poll = targetOption.getPoll();

        // O Framework assume o controle (Princípio de Hollywood)
        pollingOrchestrator.processVote(
            poll.getId().toString(),
            optionId.toString(),
            user.getId().toString()
        );

        entityManager.flush();
        entityManager.clear();

        Poll updatedPoll = pollRepository.findById(poll.getId()).orElseThrow();
        PollResponse response = PollResponse.from(updatedPoll);
        messagingTemplate.convertAndSend("/topic/group/" + groupExternalId + "/poll-update", response);
    }
}
