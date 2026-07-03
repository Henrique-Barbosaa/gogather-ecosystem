package com.role.net.roomiesapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "chores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "created_at_chore") // Just to avoid conflict if BaseEntity already maps it, but actually we can just inherit it!
    private Instant createdAtChore;

    @PrePersist
    protected void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
    }
}
