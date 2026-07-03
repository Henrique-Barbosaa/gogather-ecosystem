package com.role.net.roomiesapp.entity;

import gogather.framework.billing.dto.DebtStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "house_debts")
public class HouseDebt extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private HouseBill bill;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", nullable = true)
    private User creditor;

    @Column(name = "amount_cents", nullable = false)
    private Long amountInCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DebtStatus status = DebtStatus.PENDING;

    @Column(name = "pix_code_cache", length = 1000)
    private String pixCodeCache;
}
