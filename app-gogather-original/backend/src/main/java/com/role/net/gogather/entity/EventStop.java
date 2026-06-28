package com.role.net.gogather.entity;

import gogather.framework.sequence.SequencedItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "event_stops")
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStop extends BaseEntity implements SequencedItem {

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String category;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    private String city;

    private String state;

    @Column(name = "place_id")
    private String placeId;

    @Override
    public Integer getSequenceOrder() {
        return this.stopOrder;
    }

    @Override
    public void setSequenceOrder(Integer order) {
        this.stopOrder = order;
    }
}
