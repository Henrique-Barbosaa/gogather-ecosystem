package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.chat.ChatMessageRequest;
import com.role.net.roomiesapp.dto.chat.ChatMessageResponse;
import com.role.net.roomiesapp.dto.chat.TypingEvent;
import com.role.net.roomiesapp.entity.ChatMessage;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.ChatMessageRepository;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.orchestrator.ChatOrchestrator;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final ChatOrchestrator chatOrchestrator;
    private final ChatMessageRepository chatMessageRepository;

    public ChatWebSocketController(ChatOrchestrator chatOrchestrator, ChatMessageRepository chatMessageRepository) {
        this.chatOrchestrator = chatOrchestrator;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat/{inviteCode}/send")
    @SendTo("/topic/group/{inviteCode}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable String inviteCode,
            @Payload @Valid ChatMessageRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Map<String, Object> metadata = new HashMap<>();

        SendMessageCommand command = new SendMessageCommand(
                inviteCode,
                String.valueOf(user.getId()),
                request.content(),
                metadata
        );

        ChatMessageDTO savedDto = chatOrchestrator.processMessage(command);

        ChatMessage savedMessage = chatMessageRepository.findById(Long.parseLong(savedDto.messageId()))
                .orElseThrow(() -> new IllegalArgumentException("Message not found after save"));

        return ChatMessageResponse.from(savedMessage);
    }

    @MessageMapping("/chat/{inviteCode}/typing")
    @SendTo("/topic/group/{inviteCode}/typing")
    public TypingEvent handleTyping(
            @DestinationVariable String inviteCode,
            @Payload TypingEvent request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return new TypingEvent(user.getUsername(), request.isTyping());
    }
}
