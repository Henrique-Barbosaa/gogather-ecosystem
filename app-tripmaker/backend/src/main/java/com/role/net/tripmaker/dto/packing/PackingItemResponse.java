package com.role.net.tripmaker.dto.packing;

import com.role.net.tripmaker.entity.PackingItem;

public record PackingItemResponse(
    Long id,
    String name,
    boolean packed,
    String creatorUsername,
    String assigneeUsername
) {
    public static PackingItemResponse from(PackingItem item) {
        return new PackingItemResponse(
            item.getId(),
            item.getName(),
            item.isPacked(),
            item.getCreator() != null ? item.getCreator().getUsername() : null,
            item.getAssignee() != null ? item.getAssignee().getUsername() : null
        );
    }
}
