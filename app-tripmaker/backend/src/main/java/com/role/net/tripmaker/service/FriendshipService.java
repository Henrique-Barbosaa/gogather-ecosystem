package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.friend.FriendRequestDto;
import com.role.net.tripmaker.dto.friend.FriendResponse;
import com.role.net.tripmaker.entity.Friendship;
import com.role.net.tripmaker.entity.FriendshipStatus;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.FriendshipRepository;
import com.role.net.tripmaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FriendResponse> getUserFriends(User currentUser) {
        List<Friendship> friendships = friendshipRepository.findByRequesterIdOrReceiverId(currentUser.getId(), currentUser.getId());
        return friendships.stream().map(f -> {
            boolean isRequester = f.getRequester().getId().equals(currentUser.getId());
            User friend = isRequester ? f.getReceiver() : f.getRequester();
            
            String status;
            String requestDirection = null;

            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                status = "online";
            } else {
                status = "pending";
                requestDirection = isRequester ? "outgoing" : "incoming";
            }

            return new FriendResponse(
                f.getId(),
                friend.getId(),
                friend.getUsername(),
                friend.getEmail(),
                null,
                status,
                requestDirection,
                0
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public FriendResponse sendFriendRequest(User currentUser, FriendRequestDto request) {
        String identifier = request.email().trim();
        User target = userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByUsername(identifier))
            .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário cadastrado no banco de dados com este e-mail ou nome: " + identifier));

        if (target.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Você não pode enviar um pedido de amizade para si mesmo!");
        }

        if (friendshipRepository.findFriendshipBetween(currentUser.getId(), target.getId()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma amizade ou pedido pendente com este usuário!");
        }

        Friendship friendship = Friendship.builder()
            .requester(currentUser)
            .receiver(target)
            .status(FriendshipStatus.PENDING)
            .build();

        friendship = friendshipRepository.save(friendship);

        return new FriendResponse(
            friendship.getId(),
            target.getId(),
            target.getUsername(),
            target.getEmail(),
            null,
            "pending",
            "outgoing",
            0
        );
    }

    @Transactional
    public void acceptFriendRequest(User currentUser, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido de amizade não encontrado."));

        if (!friendship.getReceiver().getId().equals(currentUser.getId()) && !friendship.getRequester().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para alterar este pedido de amizade.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void removeOrDeclineFriendship(User currentUser, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido de amizade não encontrado."));

        if (!friendship.getReceiver().getId().equals(currentUser.getId()) && !friendship.getRequester().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para alterar este pedido de amizade.");
        }

        friendshipRepository.delete(friendship);
    }
}
