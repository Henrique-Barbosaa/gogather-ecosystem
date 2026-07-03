package com.role.net.gogather.service;

import com.role.net.gogather.entity.ChatMessage;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.MessageType;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.ChatMessageRepository;
import com.role.net.gogather.repository.GroupRepository;
import com.role.net.gogather.repository.UserRepository;
import gogather.framework.chat.core.ChatDataProvider;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import org.springframework.stereotype.Component;

@Component
public class AppChatDataProvider implements ChatDataProvider {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AppChatDataProvider(GroupRepository groupRepository, 
                               UserRepository userRepository, 
                               ChatMessageRepository chatMessageRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public boolean canAccessRoom(String roomId, String senderId) {
        return groupRepository.isGroupMemberByUserId(Long.parseLong(roomId), Long.parseLong(senderId));
    }

    @Override
    public ChatMessageDTO persistMessage(SendMessageCommand command) {
        Group group = groupRepository.findById(Long.parseLong(command.roomId()))
                .orElseThrow(() -> new ResourceNotFoundException("Group not found."));

        User sender = userRepository.findById(Long.parseLong(command.senderId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        ChatMessage message = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(command.content())
                .type(MessageType.USER)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        return new ChatMessageDTO(
                saved.getId().toString(),
                saved.getGroup().getId().toString(),
                saved.getSender().getId().toString(),
                saved.getContent(),
                saved.getType().name(),
                saved.getCreatedAt()
        );
    }
}
