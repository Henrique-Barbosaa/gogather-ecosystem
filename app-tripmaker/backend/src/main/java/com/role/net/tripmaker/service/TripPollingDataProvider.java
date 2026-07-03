package com.role.net.tripmaker.service;

import com.role.net.tripmaker.entity.Poll;
import com.role.net.tripmaker.entity.PollOption;
import com.role.net.tripmaker.entity.PollVote;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.PollOptionRepository;
import com.role.net.tripmaker.repository.PollRepository;
import com.role.net.tripmaker.repository.PollVoteRepository;
import com.role.net.tripmaker.repository.UserRepository;
import gogather.framework.polling.core.PollingDataProvider;
import gogather.framework.polling.dto.PollVoteRecord;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TripPollingDataProvider implements PollingDataProvider {

    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PollRepository pollRepository;
    private final UserRepository userRepository;

    public TripPollingDataProvider(PollOptionRepository pollOptionRepository, 
                                  PollVoteRepository pollVoteRepository, 
                                  PollRepository pollRepository,
                                  UserRepository userRepository) {
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.pollRepository = pollRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<PollVoteRecord> getUserVote(String pollId, String userId) {
        Long parsedPollId = Long.parseLong(pollId);
        Long parsedUserId = Long.parseLong(userId);
        
        Optional<PollVote> voteOpt = pollVoteRepository.findByPollIdAndUserId(parsedPollId, parsedUserId);
        
        return voteOpt.map(vote -> new PollVoteRecord(
                vote.getId().toString(),
                vote.getPollOption().getId().toString()
        ));
    }

    @Override
    public void deleteVote(String voteId) {
        pollVoteRepository.deleteById(Long.parseLong(voteId));
    }

    @Override
    public void updateVoteOption(String voteId, String newOptionId) {
        PollVote vote = pollVoteRepository.findById(Long.parseLong(voteId))
                .orElseThrow(() -> new ResourceNotFoundException("Voto não encontrado"));
                
        PollOption newOption = pollOptionRepository.findById(Long.parseLong(newOptionId))
                .orElseThrow(() -> new ResourceNotFoundException("Nova opção não encontrada"));
                
        vote.setPollOption(newOption);
        pollVoteRepository.save(vote);
    }

    @Override
    public void createVote(String pollId, String optionId, String userId) {
        Poll poll = pollRepository.findById(Long.parseLong(pollId))
                .orElseThrow(() -> new ResourceNotFoundException("Enquete não encontrada"));
                
        PollOption option = pollOptionRepository.findById(Long.parseLong(optionId))
                .orElseThrow(() -> new ResourceNotFoundException("Opção não encontrada"));
                
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        PollVote newVote = PollVote.builder()
                .poll(poll)
                .pollOption(option)
                .user(user)
                .build();
                
        pollVoteRepository.save(newVote);
    }

    @Override
    public void incrementOption(String optionId) {
        pollOptionRepository.incrementVote(Long.parseLong(optionId));
    }

    @Override
    public void decrementOption(String optionId) {
        pollOptionRepository.decrementVote(Long.parseLong(optionId));
    }
}
