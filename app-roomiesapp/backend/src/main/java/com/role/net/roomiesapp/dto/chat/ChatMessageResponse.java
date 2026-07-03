package com.role.net.roomiesapp.dto.chat;

import com.role.net.roomiesapp.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(
    String id,
    String senderId,
    String senderName,
    String content,
    String type,
    Instant createdAt,
    PollResponse poll
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId().toString(),
            message.getSender() != null ? message.getSender().getId().toString() : null,
            message.getSender() != null ? message.getSender().getName() : "Sistema",
            message.getContent(),
            message.getType().name(),
            message.getCreatedAt(),
            message.getPoll() != null ? PollResponse.from(message.getPoll()) : null
        );
    }
}
