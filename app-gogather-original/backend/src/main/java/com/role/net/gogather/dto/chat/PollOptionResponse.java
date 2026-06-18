package com.role.net.gogather.dto.chat;

import com.role.net.gogather.entity.PollOption;

import java.util.List;
import com.role.net.gogather.entity.PollVote;

public record PollOptionResponse(
    Long id,
    String text,
    String placeId,
    int votes,
    List<Long> voterIds
) {
    public static PollOptionResponse from(PollOption option) {
        List<Long> voterIds = option.getUserVotes() != null
            ? option.getUserVotes().stream().map(vote -> vote.getUser().getId()).toList()
            : List.of();

        return new PollOptionResponse(
            option.getId(),
            option.getText(),
            option.getPlaceId(),
            option.getVotes(),
            voterIds
        );
    }
}
