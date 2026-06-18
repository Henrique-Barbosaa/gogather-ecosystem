package com.role.net.gogather.entity;

import java.util.HashSet;
import java.util.Set;

import com.role.net.gogather.enums.GroupMemberStatus;
import com.role.net.gogather.enums.GroupRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "group_members")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberStatus status;

    @ManyToOne
    @JoinColumn(name = "invited_by_id", nullable = true)
    private User invitedBy;

    @Builder.Default
    @OneToMany(mappedBy = "payer")
    private Set<ExpenseContribution> expenseContributions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "debtor")
    private Set<ExpenseDistribution> expenseDistributions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "creditor")
    private Set<ExpenseDistribution> expensesReceived = new HashSet<>();
}
