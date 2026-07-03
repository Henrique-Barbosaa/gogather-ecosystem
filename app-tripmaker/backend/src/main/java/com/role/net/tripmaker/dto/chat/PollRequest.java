package com.role.net.tripmaker.dto.chat;

import java.util.List;

public record PollRequest(
    String question,
    List<String> options
) {}
