package com.role.net.tripmaker.dto.chat;

public record TypingEvent(
    String username,
    boolean isTyping
) {}
