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

import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class PollService {

    private final PollOptionRepository pollOptionRepository;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public PollService(PollOptionRepository pollOptionRepository, PollRepository pollRepository, PollVoteRepository pollVoteRepository, SimpMessagingTemplate messagingTemplate) {
        this.pollOptionRepository = pollOptionRepository;
        this.pollRepository = pollRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void vote(Long optionId, UUID groupExternalId, User user) {
        PollOption targetOption = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opção de enquete não encontrada"));

        Poll poll = targetOption.getPoll();

        Optional<PollVote> existingVoteOpt = pollVoteRepository.findByPollIdAndUserId(poll.getId(), user.getId());

        if (existingVoteOpt.isPresent()) {
            PollVote existingVote = existingVoteOpt.get();
            if (existingVote.getPollOption().getId().equals(optionId)) {
                pollVoteRepository.delete(existingVote);
                pollOptionRepository.decrementVote(optionId);
            } else {
                pollOptionRepository.decrementVote(existingVote.getPollOption().getId());
                existingVote.setPollOption(targetOption);
                pollVoteRepository.save(existingVote);
                pollOptionRepository.incrementVote(optionId);
            }
        } else {
            PollVote newVote = PollVote.builder()
                .poll(poll)
                .pollOption(targetOption)
                .user(user)
                .build();
            pollVoteRepository.save(newVote);
            pollOptionRepository.incrementVote(optionId);
        }

        entityManager.flush();
        entityManager.clear();

        Poll updatedPoll = pollRepository.findById(poll.getId()).orElseThrow();
        PollResponse response = PollResponse.from(updatedPoll);
        messagingTemplate.convertAndSend("/topic/group/" + groupExternalId + "/poll-update", response);
    }
}
