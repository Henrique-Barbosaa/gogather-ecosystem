package com.role.net.roomiesapp.dto.chore;

import com.role.net.roomiesapp.entity.Chore;

public record ChoreResponse(
    Long id,
    String description,
    boolean completed,
    String creatorUsername,
    String assigneeUsername
) {
    public static ChoreResponse from(Chore chore) {
        return new ChoreResponse(
            chore.getId(),
            chore.getDescription(),
            chore.isCompleted(),
            chore.getCreator() != null ? chore.getCreator().getUsername() : null,
            chore.getAssignee() != null ? chore.getAssignee().getUsername() : null
        );
    }
}
