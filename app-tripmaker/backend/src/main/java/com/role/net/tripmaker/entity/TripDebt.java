package com.role.net.tripmaker.entity;

import gogather.framework.billing.dto.DebtStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_debts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDebt extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private TripExpense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    @Column(name = "amount_in_cents", nullable = false)
    private Long amountInCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DebtStatus status = DebtStatus.PENDING;
}
