package gogather.framework.chat.dto;

import java.time.Instant;

public record ChatMessageDTO(
    String messageId, 
    String roomId, 
    String senderId, 
    String content, 
    String type,
    Instant createdAt
) {}
