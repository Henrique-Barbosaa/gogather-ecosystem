package com.role.net.tripmaker.dto.chat;

import com.role.net.tripmaker.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(
    Long id,
    Long senderId,
    String senderName,
    String senderAvatar,
    String content,
    String type,
    Instant createdAt,
    PollResponse poll
) {
    public static ChatMessageResponse from(ChatMessage message) {
        String name = message.getSender() != null ? message.getSender().getName() : "Sistema";
        String avatar = "https://ui-avatars.com/api/?name=" + (name != null ? name.replace(" ", "+") : "User") + "&background=random&color=fff&size=256&font-size=0.4";
        return new ChatMessageResponse(
            message.getId(),
            message.getSender() != null ? message.getSender().getId() : null,
            name,
            avatar,
            message.getContent(),
            message.getType().name(),
            message.getCreatedAt(),
            message.getPoll() != null ? PollResponse.from(message.getPoll()) : null
        );
    }
}
