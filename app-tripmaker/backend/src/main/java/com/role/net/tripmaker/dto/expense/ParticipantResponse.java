package com.role.net.tripmaker.dto.expense;

public record ParticipantResponse(
    Long userId,
    String username,
    String displayName
) {}
