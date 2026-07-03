package com.role.net.roomiesapp.dto.chat;

import java.util.List;

public record PollRequest(
    String question,
    List<String> options
) {}
