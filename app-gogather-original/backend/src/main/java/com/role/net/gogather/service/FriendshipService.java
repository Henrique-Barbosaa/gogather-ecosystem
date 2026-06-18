package com.role.net.gogather.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.dto.friendship.FriendshipResponse;
import com.role.net.gogather.dto.friendship.FriendshipSimpleResponse;
import com.role.net.gogather.entity.Friendship;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.FriendshipStatus;
import com.role.net.gogather.exception.InvalidDataException;
import com.role.net.gogather.exception.InvalidRequestException;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.FriendshipRepository;
import com.role.net.gogather.repository.UserRepository;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(
        FriendshipRepository friendshipRepository,
        UserRepository userRepository
    ) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public FriendshipResponse details(Long requesterId, UUID friendshipExternalId) {
        Friendship friendship = friendshipRepository.findByExternalId(friendshipExternalId)
            .orElseThrow(() -> new ResourceNotFoundException("Friendship nor found."));
        return FriendshipResponse.from(friendship);
    }

    public List<FriendshipSimpleResponse> friends(Long loggedUserId) {
        List<Friendship> friends = friendshipRepository
            .findByUserIdAndStatus(loggedUserId, FriendshipStatus.ACCEPTED);
        return friends.stream()
            .map(obj -> {
                User target;

                if(obj.getRequester().getId().equals(loggedUserId))
                    target = obj.getReceiver();
                else
                    target = obj.getRequester();

                return new FriendshipSimpleResponse(
                    obj.getExternalId(),
                    target.getExternalId(),
                    target.getUsername(),
                    target.getDisplayName(),
                    obj.getStatus());
            })
            .toList();
    }

    public List<Friendship> pending(Long loggedId) {
        List<Friendship> pending = friendshipRepository
            .findByReceiverIdAndStatus(loggedId, FriendshipStatus.PENDING);
        return pending;
    }

    public Friendship friendship(UUID loggedExternalId, UUID externalId) {
        Optional<Friendship> friendship = friendshipRepository
            .findFriendshipBetweenUsers(loggedExternalId, externalId);
        if(!friendship.isPresent()) return null;
        return friendship.get();
    }

    @Transactional
    public Friendship send(Long requesterId, UUID receiverExternalId) {

        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Requester not found."));
        User receiver = userRepository.findByExternalId(receiverExternalId)
            .orElseThrow(() -> new ResourceNotFoundException("Receiver not found."));

        if (requester.getId().equals(receiver.getId()))
            throw new InvalidRequestException("Requester and receiver cannot be the same");

        Optional<Friendship> temp = friendshipRepository
            .findFriendshipBetweenUsers(requester.getId(), receiver.getId());
        if(temp.isPresent()){
            Friendship obj = temp.get();

            switch (obj.getStatus()) {
               	case ACCEPTED:
                    throw new InvalidRequestException("Friendship request already accepted.");
                case PENDING:
                    throw new InvalidRequestException("Friendship request already pending.");
                case REJECTED:
                    if(obj.getRequester().getId().equals(requesterId)){
                        Instant now = Instant.now();
                        if(obj.getAllowSendAt() != null && obj.getAllowSendAt().isAfter(now))
                            throw new InvalidRequestException(
                                obj.getReceiver().getDisplayName() +
                                " rejected your request before! Wait " +
                                Duration.between(now, obj.getAllowSendAt()).toMinutes() +
                                " minutes."
                            );
                    } else obj.setDaysInterval(0);
                    obj.setRequester(requester);
                    obj.setReceiver(receiver);
                    obj.setAllowSendAt(null);
                    obj.setFriendshipDate(null);
                    obj.setStatus(FriendshipStatus.PENDING);

                    return friendshipRepository.save(obj);
                case UNFRIENDED:
                    obj.setRequester(requester);
                    obj.setReceiver(receiver);
                    obj.setDaysInterval(0);
                    obj.setAllowSendAt(null);
                    obj.setFriendshipDate(null);
                    obj.setStatus(FriendshipStatus.PENDING);

                    return friendshipRepository.save(obj);
               	default:
              		throw new InvalidDataException("Friendship status is invalid.");
            }
        }

        Friendship friendship = Friendship.builder()
            .requester(requester)
            .receiver(receiver)
            .friendshipDate(null)
            .status(FriendshipStatus.PENDING)
            .daysInterval(0)
            .build();

        return friendshipRepository.save(friendship);
    }

    @Transactional
    public FriendshipResponse accept(Long receiverId, UUID friendshipExternalId) {
        Friendship friendship = friendshipRepository.findByExternalIdAndReceiverId(friendshipExternalId, receiverId)
            .orElseThrow(() -> new ResourceNotFoundException("Friendship not found or you don't have permission to accept it"));

        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new InvalidRequestException("Friendship isn't pending");

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setFriendshipDate(LocalDate.now());
        friendship.setDaysInterval(0);

        return FriendshipResponse.from(friendship);
    }

    @Transactional
    public FriendshipResponse refuse(Long receiverId, UUID friendshipExternalId) {
        Friendship friendship = friendshipRepository.findByExternalIdAndReceiverId(friendshipExternalId, receiverId)
            .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new InvalidRequestException("Friendship isn't pending");

        int currentDaysInterval = friendship.getDaysInterval();
        int nextInterval = currentDaysInterval == 0 ? 1 : currentDaysInterval*2;

        friendship.setDaysInterval(Math.min(nextInterval, 30));
        friendship.setAllowSendAt(
            Instant
                .now()
                .plus(friendship.getDaysInterval(), ChronoUnit.DAYS)
        );
        friendship.setStatus(FriendshipStatus.REJECTED);

        return FriendshipResponse.from(friendship);
    }

    @Transactional
    public FriendshipResponse unfriend(Long loggedUserId, UUID friendshipExternalId) {
        Friendship friendship = friendshipRepository.findByExternalId(friendshipExternalId)
            .orElseThrow(() -> new ResourceNotFoundException("Friendship not found."));

        boolean isRequester = friendship.getRequester().getId().equals(loggedUserId);
        boolean isResolver = friendship.getReceiver().getId().equals(loggedUserId);

        if(!isRequester && !isResolver)
            throw new InvalidRequestException("You don't have permission to modify this friendship.");
        if(friendship.getStatus() != FriendshipStatus.ACCEPTED)
            throw new InvalidRequestException("You and this user aren't friends.");

        friendship.setStatus(FriendshipStatus.UNFRIENDED);
        friendship.setFriendshipDate(null);

        return FriendshipResponse.from(friendship);
    }
}
