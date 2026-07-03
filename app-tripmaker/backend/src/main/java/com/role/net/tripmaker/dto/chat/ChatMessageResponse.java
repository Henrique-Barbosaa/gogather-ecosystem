package com.role.net.tripmaker.dto.chat;

import com.role.net.tripmaker.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(
    Long id,
    Long senderId,
    String senderName,
    String content,
    String type,
    Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getSender() != null ? message.getSender().getId() : null,
            message.getSender() != null ? message.getSender().getName() : "Sistema",
            message.getContent(),
            message.getType().name(),
            message.getCreatedAt()
        );
    }
}
