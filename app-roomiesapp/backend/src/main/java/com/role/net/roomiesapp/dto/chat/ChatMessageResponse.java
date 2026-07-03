package com.role.net.roomiesapp.dto.chat;

import com.role.net.roomiesapp.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(
        String id,
        String content,
        String senderId,
        String senderName,
        String type,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId().toString(),
                message.getContent(),
                message.getSender() != null ? message.getSender().getId().toString() : null,
                message.getSender() != null ? message.getSender().getName() : "System",
                message.getType().name(),
                message.getCreatedAt()
        );
    }
}
