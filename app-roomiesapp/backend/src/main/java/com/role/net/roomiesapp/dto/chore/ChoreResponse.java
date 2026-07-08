package com.role.net.roomiesapp.dto.chore;

import com.role.net.roomiesapp.entity.Chore;
import java.time.LocalDate;

public record ChoreResponse(
    Long id,
    String title,
    String description,
    boolean completed,
    String creatorUsername,
    String assigneeUsername,
    LocalDate dueDate
) {
    public static ChoreResponse from(Chore chore) {
        return new ChoreResponse(
            chore.getId(),
            chore.getTitle(),
            chore.getDescription(),
            chore.isCompleted(),
            chore.getCreator() != null ? chore.getCreator().getUsername() : null,
            chore.getAssignee() != null ? chore.getAssignee().getUsername() : null,
            chore.getDueDate()
        );
    }
}
