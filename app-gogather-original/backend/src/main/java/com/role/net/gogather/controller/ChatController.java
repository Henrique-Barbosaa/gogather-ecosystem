package com.role.net.gogather.controller;

import com.role.net.gogather.dto.chat.ChatMessageResponse;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.ChatService;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{externalId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getChatHistory(
            @PathVariable UUID externalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ChatMessageResponse> history = chatService.getMessagesByGroup(externalId, user.getId(), pageable);

        return ResponseEntity.ok(history);
    }
}
