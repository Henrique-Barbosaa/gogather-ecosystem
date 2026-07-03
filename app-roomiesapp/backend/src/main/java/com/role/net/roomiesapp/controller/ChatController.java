package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.chat.ChatMessageResponse;
import com.role.net.roomiesapp.entity.ChatMessage;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.ChatMessageRepository;
import gogather.framework.chat.core.ChatDataProvider;
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

    private final ChatMessageRepository chatMessageRepository;
    private final ChatDataProvider chatDataProvider;

    public ChatController(ChatMessageRepository chatMessageRepository, ChatDataProvider chatDataProvider) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatDataProvider = chatDataProvider;
    }

    @GetMapping("/{inviteCode}/chat")
    public ResponseEntity<Page<ChatMessageResponse>> getChatHistory(
            @PathVariable String inviteCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        if (!chatDataProvider.canAccessRoom(inviteCode, String.valueOf(user.getId()))) {
            return ResponseEntity.status(403).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChatMessage> history = chatMessageRepository.findByGroupInviteCodeOrderByCreatedAtDesc(inviteCode, pageable);

        return ResponseEntity.ok(history.map(ChatMessageResponse::from));
    }
}
