package gogather.framework.chat.dto;

import java.util.Map;

public record SendMessageCommand(
    String roomId,
    String senderId,
    String content,
    Map<String, Object> metadata
) {}
