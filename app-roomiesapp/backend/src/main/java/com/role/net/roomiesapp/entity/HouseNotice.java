package com.role.net.roomiesapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "house_notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseNotice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at_notice")
    private Instant createdAtNotice;

    @PrePersist
    protected void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
    }
}
