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
import com.role.net.tripmaker.entity.Group;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups/{idOrCode}/chat")
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
            @PathVariable String idOrCode,
            @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> metadata = new HashMap<>();

        SendMessageCommand command = new SendMessageCommand(
                idOrCode,
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
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String idOrCode,
            @AuthenticationPrincipal User user) {
        
        Group group = null;
        boolean isMember = false;
        try {
            Long id = Long.parseLong(idOrCode);
            group = groupRepository.findById(id).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMember(id, user.getId());
            }
        } catch (NumberFormatException e) {
            // Not numeric, lookup by invite code
        }
        if (group == null) {
            group = groupRepository.findByInviteCode(idOrCode).orElse(null);
            if (group != null) {
                isMember = groupRepository.isGroupMemberByInviteCode(idOrCode, user.getId());
            }
        }
        if (group == null || !isMember) {
            throw new SecurityException("Você não é membro desta viagem.");
        }

        List<ChatMessageResponse> messages = chatMessageRepository
                .findByGroupIdOrderByCreatedAtAsc(group.getId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();

        return ResponseEntity.ok(messages);
    }
}
