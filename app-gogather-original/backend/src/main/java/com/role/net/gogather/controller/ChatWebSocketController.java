package com.role.net.gogather.controller;

import com.role.net.gogather.dto.chat.ChatMessageRequest;
import com.role.net.gogather.dto.chat.ChatMessageResponse;
import com.role.net.gogather.dto.chat.TypingEvent;
import com.role.net.gogather.entity.ChatMessage;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.GroupRepository;
import com.role.net.gogather.service.ChatService;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final GroupRepository groupRepository;

    public ChatWebSocketController(ChatService chatService, GroupRepository groupRepository) {
        this.chatService = chatService;
        this.groupRepository = groupRepository;
    }

    @MessageMapping("/chat/{externalId}/send")
    @SendTo("/topic/group/{externalId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable String externalId,
            @Payload @Valid ChatMessageRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Group group = groupRepository.findByExternalId(UUID.fromString(externalId))
                .orElseThrow(() -> new ResourceNotFoundException("Group not found."));

        ChatMessage savedMessage = chatService.saveMessage(group.getId(), user.getId(), request);
        return ChatMessageResponse.from(savedMessage);
    }

	@MessageMapping("/chat/{externalId}/typing")
    @SendTo("/topic/group/{externalId}/typing")
    public TypingEvent handleTyping(
            @DestinationVariable String externalId,
            @Payload TypingEvent request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return new TypingEvent(user.getUsername(), request.isTyping());
    }
}
