package com.role.net.gogather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    sequenceName = "seq_expense_contribution",
    allocationSize = 5
)
@Table(name = "expense_contribution")
@Builder
public class ExpenseContribution extends BaseEntity {

    // O valor vai ser salvo em centavos
    @NotNull(message = "Expense contribution value cannot be null.")
    @Column(nullable = false)
    private Long value;

    @NotNull(message = "Payed expense split must have a parent expense.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense parentExpense;

    @NotNull(message = "Payed expense split must have an user.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private GroupMember payer;
}
