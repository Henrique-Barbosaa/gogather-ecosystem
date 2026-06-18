package com.role.net.gogather.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "event_groups")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private Set<GroupMember> members = new HashSet<>();

    @Column(
        name = "invite_code",
        unique = true,
        updatable = false,
        nullable = false
    )
    private String inviteCode;

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("stopOrder ASC")
    @Builder.Default
    private List<EventStop> eventStops = new ArrayList<>();

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<GroupImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    @Builder.Default
    private Set<Expense> expenses = new HashSet<>();

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @PrePersist
    public void generateInviteCode() {
        if (this.inviteCode == null || this.inviteCode.isEmpty()) {
            this.inviteCode = UUID.randomUUID()
                .toString()
                .toUpperCase()
                .replaceAll("-", "")
                .substring(0, 8);
        }
    }
}
