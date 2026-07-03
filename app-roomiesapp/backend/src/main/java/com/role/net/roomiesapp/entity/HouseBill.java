package com.role.net.roomiesapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "house_bills")
public class HouseBill extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type", nullable = false)
    private BillType billType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_interval")
    private RecurrenceInterval recurrenceInterval = RecurrenceInterval.NONE;

    @Column(name = "custom_interval_days")
    private Integer customIntervalDays;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", nullable = true)
    private User contributor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "bill_participants",
        joinColumns = @JoinColumn(name = "bill_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();
}
