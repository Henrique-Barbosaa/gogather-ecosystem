package com.role.net.roomiesapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "shopping_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String quantity;

    @Builder.Default
    @Column(nullable = false)
    private boolean bought = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @Column(name = "created_at_item") // Just to avoid conflict with BaseEntity if any
    private Instant createdAtItem;

    @PrePersist
    protected void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
    }
}
