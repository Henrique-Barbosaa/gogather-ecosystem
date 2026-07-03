package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    Optional<PollVote> findByPollIdAndUserId(Long pollId, Long userId);
}
