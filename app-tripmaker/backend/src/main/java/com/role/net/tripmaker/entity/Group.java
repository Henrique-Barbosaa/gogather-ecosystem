package com.role.net.tripmaker.entity;

import gogather.framework.group.jpa.domain.BaseGroup;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trips")
public class Group extends BaseGroup {

    @Column(name = "destination", length = 200)
    private String destination;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "max_travelers", nullable = false)
    private Integer maxTravelers = 30;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TripExpense> expenses = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ItineraryEvent> itineraryEvents = new java.util.ArrayList<>();

    public void addMember(User user, gogather.framework.group.jpa.domain.GroupRole role) {
        gogather.framework.group.jpa.domain.GroupMember member = new gogather.framework.group.jpa.domain.GroupMember();
        member.setGroup(this);
        member.setUser(user);
        member.setRole(role);
        if (getMembers() == null) {
            setMembers(new java.util.ArrayList<>());
        }
        getMembers().add(member);
    }

    public void addMember(gogather.framework.group.jpa.domain.GroupMember member) {
        member.setGroup(this);
        if (getMembers() == null) {
            setMembers(new java.util.ArrayList<>());
        }
        getMembers().add(member);
    }
}