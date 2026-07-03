package com.role.net.tripmaker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_expenses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripExpense extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Group trip;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ExpenseCategory category = ExpenseCategory.OUTROS;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripExpenseContribution> contributions = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripExpenseParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripDebt> debts = new ArrayList<>();
}
