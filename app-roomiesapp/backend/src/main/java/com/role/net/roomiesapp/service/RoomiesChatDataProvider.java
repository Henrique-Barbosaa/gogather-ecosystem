package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.entity.ChatMessage;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.MessageType;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.ChatMessageRepository;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.UserRepository;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.core.ChatDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomiesChatDataProvider implements ChatDataProvider {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomiesGroupRepository groupRepository;
    private final UserRepository userRepository;

    public RoomiesChatDataProvider(ChatMessageRepository chatMessageRepository, RoomiesGroupRepository groupRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean canAccessRoom(String roomId, String senderId) {
        return groupRepository.isGroupMemberByInviteCode(roomId, Long.parseLong(senderId));
    }

    @Override
    @Transactional
    public ChatMessageDTO persistMessage(SendMessageCommand command) {
        Group group = groupRepository.findByInviteCode(command.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User sender = null;
        if (command.senderId() != null) {
            sender = userRepository.findById(Long.parseLong(command.senderId()))
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(command.content())
                .type(sender == null ? MessageType.SYSTEM : MessageType.USER)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return new ChatMessageDTO(
                savedMessage.getId().toString(),
                savedMessage.getGroup().getInviteCode(),
                savedMessage.getSender() != null ? savedMessage.getSender().getId().toString() : null,
                savedMessage.getContent(),
                savedMessage.getType().name(),
                savedMessage.getCreatedAt()
        );
    }
}