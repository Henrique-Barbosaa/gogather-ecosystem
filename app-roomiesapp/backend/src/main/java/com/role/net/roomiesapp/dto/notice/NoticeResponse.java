package com.role.net.roomiesapp.dto.notice;

import com.role.net.roomiesapp.entity.HouseNotice;
import java.time.Instant;

public record NoticeResponse(
    Long id,
    String title,
    String content,
    String creatorUsername,
    Instant createdAt
) {
    public static NoticeResponse from(HouseNotice notice) {
        return new NoticeResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getCreator() != null ? notice.getCreator().getUsername() : null,
            notice.getCreatedAtNotice()
        );
    }
}
