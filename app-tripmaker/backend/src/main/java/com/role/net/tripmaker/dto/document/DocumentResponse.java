package com.role.net.tripmaker.dto.document;

import com.role.net.tripmaker.entity.TripDocument;
import java.time.Instant;

public record DocumentResponse(
    Long id,
    String title,
    String url,
    String category,
    String creatorUsername,
    Instant createdAt
) {
    public static DocumentResponse from(TripDocument doc) {
        return new DocumentResponse(
            doc.getId(),
            doc.getTitle(),
            doc.getUrl(),
            doc.getCategory(),
            doc.getCreator() != null ? doc.getCreator().getUsername() : null,
            doc.getCreatedAtDoc()
        );
    }
}
