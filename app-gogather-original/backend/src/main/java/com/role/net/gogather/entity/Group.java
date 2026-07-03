package com.role.net.gogather.entity;

import gogather.framework.group.jpa.domain.BaseGroup;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "event_groups")
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseGroup {

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("stopOrder ASC")
    private List<EventStop> eventStops = new ArrayList<>();

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<GroupImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private Set<Expense> expenses = new HashSet<>();

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;
}