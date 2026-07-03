package com.role.net.roomiesapp.dto.chat;

public record TypingEvent(
    String username,
    boolean isTyping
) {}
