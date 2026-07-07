package com.role.net.tripmaker.entity;

import gogather.framework.sequence.SequencedItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "itinerary_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryEvent extends BaseEntity implements SequencedItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String day;

    @Column(length = 20)
    private String time;

    @Column(length = 50)
    private String category;

    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    @Column(length = 300)
    private String location;

    @Column(name = "cost_estimate_cents")
    private Long costEstimateCents;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

}
