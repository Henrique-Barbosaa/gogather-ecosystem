package com.role.net.gogather.entity;

import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.group.jpa.domain.GroupMember;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
    name = "id_generator",
    sequenceName = "seq_expense_distribution",
    allocationSize = 10
)
@Table(name = "expense_distribution")
@Builder
public class ExpenseDistribution extends BaseEntity {

    // O valor vai ser salvo em centavos
    @NotNull(message = "Expense split value cannot be null")
    @Column(name = "\"value\"", nullable = false)
    private Long value;

    @NotNull(message = "Expense split status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @NotNull(message = "Expense split must have a debtor")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", nullable = false)
    private GroupMember debtor;

    @NotNull(message = "Expense split must have a creditor")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", nullable = false)
    private GroupMember creditor;

    @NotNull(message = "Expense split must have a parent expense")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense parentExpense;

}
