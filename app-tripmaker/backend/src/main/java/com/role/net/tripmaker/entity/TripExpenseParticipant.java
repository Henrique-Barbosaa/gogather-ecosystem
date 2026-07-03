package com.role.net.tripmaker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_expense_participants")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripExpenseParticipant extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private TripExpense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;
}
