package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.chat.ChatMessageRequest;
import com.role.net.tripmaker.dto.chat.ChatMessageResponse;
import com.role.net.tripmaker.entity.ChatMessage;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.ChatMessageRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.orchestrator.ChatOrchestrator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/groups/{inviteCode}/chat")
public class ChatController {

    private final ChatOrchestrator chatOrchestrator;
    private final ChatMessageRepository chatMessageRepository;
    private final TripGroupRepository groupRepository;

    public ChatController(
            ChatOrchestrator chatOrchestrator,
            ChatMessageRepository chatMessageRepository,
            TripGroupRepository groupRepository) {
        this.chatOrchestrator = chatOrchestrator;
        this.chatMessageRepository = chatMessageRepository;
        this.groupRepository = groupRepository;
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable String inviteCode,
            @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> metadata = new HashMap<>();

        SendMessageCommand command = new SendMessageCommand(
                inviteCode,
                String.valueOf(user.getId()),
                request.content(),
                metadata
        );

        ChatMessageDTO savedDto = chatOrchestrator.processMessage(command);

        ChatMessage savedMessage = chatMessageRepository.findById(Long.parseLong(savedDto.messageId()))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found after save"));

        return ResponseEntity.ok(ChatMessageResponse.from(savedMessage));
    }

    @GetMapping
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        
        if (!groupRepository.isGroupMemberByInviteCode(inviteCode, user.getId())) {
            throw new SecurityException("Você não é membro desta viagem.");
        }

        Page<ChatMessageResponse> messages = chatMessageRepository
                .findByGroupInviteCodeOrderByCreatedAtDesc(inviteCode, pageable)
                .map(ChatMessageResponse::from);

        return ResponseEntity.ok(messages);
    }
}
