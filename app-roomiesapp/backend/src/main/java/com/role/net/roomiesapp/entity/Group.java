package com.role.net.roomiesapp.entity;

import gogather.framework.group.jpa.domain.BaseGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "households")
public class Group extends BaseGroup {

    @Column(name = "external_id", unique = true, nullable = false, updatable = false)
    private UUID externalId = UUID.randomUUID();

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "monthly_rent_cents")
    private Long monthlyRentCents;

    @Column(name = "max_occupants", nullable = false)
    private Integer maxOccupants = 8;
}