package com.role.net.tripmaker.dto.chat;

import com.role.net.tripmaker.entity.Poll;
import java.util.List;

public record PollResponse(
    Long id,
    List<PollOptionResponse> options
) {
    public static PollResponse from(Poll poll) {
        if (poll == null) {
            return null;
        }
        List<PollOptionResponse> options = poll.getOptions().stream()
            .map(PollOptionResponse::from)
            .toList();

        return new PollResponse(poll.getId(), options);
    }
}
