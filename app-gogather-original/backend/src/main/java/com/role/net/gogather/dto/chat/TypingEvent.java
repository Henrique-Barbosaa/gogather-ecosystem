package com.role.net.gogather.dto.chat;

public record TypingEvent(
    String senderName,
    boolean isTyping
) {}
