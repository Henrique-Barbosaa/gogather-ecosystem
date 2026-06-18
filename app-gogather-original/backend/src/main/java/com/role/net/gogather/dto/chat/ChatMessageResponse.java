package com.role.net.gogather.dto.chat;

import com.role.net.gogather.entity.ChatMessage;
import com.role.net.gogather.enums.MessageType;
import java.time.Instant;

public record ChatMessageResponse(
    String content,
    String senderName,
    MessageType type,
    Instant createdAt,
    PollResponse poll
) {
    public static ChatMessageResponse from(ChatMessage message) {
        String senderName = message.getType() == MessageType.USER && message.getSender() != null
                            ? message.getSender().getDisplayName()
                            : "GoGather AI";

        return new ChatMessageResponse(
            message.getContent(),
            senderName,
            message.getType(),
            message.getCreatedAt(),
            PollResponse.from(message.getPoll())
        );
    }
}
