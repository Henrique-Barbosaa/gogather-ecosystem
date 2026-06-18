package com.role.net.gogather.dto.chat;

public record AiMentionEvent(
    Long groupId,
    String content
) {}
