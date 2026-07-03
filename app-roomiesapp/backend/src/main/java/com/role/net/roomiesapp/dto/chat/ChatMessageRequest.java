package com.role.net.roomiesapp.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "Message content cannot be empty")
        String content
) {
}
