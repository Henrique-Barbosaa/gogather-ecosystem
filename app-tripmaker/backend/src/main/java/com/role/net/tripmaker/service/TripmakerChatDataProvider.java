package com.role.net.tripmaker.service;

import com.role.net.tripmaker.entity.ChatMessage;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.MessageType;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.ChatMessageRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;
import gogather.framework.chat.core.ChatDataProvider;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import org.springframework.stereotype.Service;

@Service
public class TripmakerChatDataProvider implements ChatDataProvider {

    private final TripGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public TripmakerChatDataProvider(
            TripGroupRepository groupRepository,
            UserRepository userRepository,
            ChatMessageRepository chatMessageRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public boolean canAccessRoom(String roomId, String senderId) {
        // roomId is the inviteCode for Tripmaker
        return groupRepository.isGroupMemberByInviteCode(roomId, Long.parseLong(senderId));
    }

    @Override
    public ChatMessageDTO persistMessage(SendMessageCommand command) {
        Group group = groupRepository.findByInviteCode(command.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada."));

        User sender = null;
        if (command.senderId() != null) {
            sender = userRepository.findById(Long.parseLong(command.senderId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        }

        ChatMessage message = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(command.content())
                .type(sender == null ? MessageType.SYSTEM : MessageType.USER)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        return new ChatMessageDTO(
                saved.getId().toString(),
                saved.getGroup().getInviteCode(),
                saved.getSender() != null ? saved.getSender().getId().toString() : null,
                saved.getContent(),
                saved.getType().name(),
                saved.getCreatedAt()
        );
    }
}
