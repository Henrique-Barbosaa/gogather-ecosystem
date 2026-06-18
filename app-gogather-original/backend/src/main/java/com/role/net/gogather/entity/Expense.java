package com.role.net.gogather.entity;

import java.util.HashSet;
import java.util.Set;

import com.role.net.gogather.enums.ExpenseStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
    name = "id_generator",
    sequenceName = "seq_expense",
    allocationSize = 1
)
@Table(name = "expense")
@Builder
public class Expense extends BaseEntity {

    @Column(nullable = true)
    private String description;

    // O valor vai ser salvo em centavos
    @NotNull(message = "Expense total value cannot be null!")
    @Column(nullable = false)
    private Long totalValue;

    @NotNull(message = "Expense status cannot be null!")
    @Column(nullable = false)
    private ExpenseStatus status;

    @NotNull(message = "The expense must have a group!")
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @Builder.Default
    @OneToMany(mappedBy = "parentExpense", cascade = CascadeType.PERSIST)
    private Set<ExpenseContribution> expenseContributions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "parentExpense", cascade = CascadeType.PERSIST)
    private Set<ExpenseDistribution> expenseDistributions = new HashSet<>();
}
