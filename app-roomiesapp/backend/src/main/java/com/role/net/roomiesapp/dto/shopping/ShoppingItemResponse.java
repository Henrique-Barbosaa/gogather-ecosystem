package com.role.net.roomiesapp.dto.shopping;

import com.role.net.roomiesapp.entity.ShoppingItem;

public record ShoppingItemResponse(
    Long id,
    String name,
    String quantity,
    boolean bought,
    String creatorUsername,
    String buyerUsername
) {
    public static ShoppingItemResponse from(ShoppingItem item) {
        return new ShoppingItemResponse(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.isBought(),
            item.getCreator() != null ? item.getCreator().getUsername() : null,
            item.getBuyer() != null ? item.getBuyer().getUsername() : null
        );
    }
}
