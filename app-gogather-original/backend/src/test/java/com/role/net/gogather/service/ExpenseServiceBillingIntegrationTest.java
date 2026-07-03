package com.role.net.gogather.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.dto.expense.ExpenseAutoCreationRequest;
import com.role.net.gogather.dto.expense.ExpenseContributionRequest;
import com.role.net.gogather.dto.expense.ExpenseDistributionRequest;
import com.role.net.gogather.dto.expense.ExpenseManualCreationRequest;
import com.role.net.gogather.entity.Expense;
import com.role.net.gogather.entity.Group;
import gogather.framework.group.jpa.domain.GroupMember;
import com.role.net.gogather.entity.PixInfo;
import com.role.net.gogather.entity.User;
import gogather.framework.group.jpa.domain.GroupRole;
import gogather.framework.billing.dto.DebtStatus;
import com.role.net.gogather.repository.GroupRepository;
import com.role.net.gogather.repository.UserRepository;

@SpringBootTest
@Transactional
class ExpenseServiceBillingIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private User userA;
    private User userB;
    private User userC;
    private Group group;
    private GroupMember memberA;
    private GroupMember memberB;
    private GroupMember memberC;

    @BeforeEach
    void setup() {
        userA = createUser("usera", "usera@test.com", "User A", "pix-a");
        userB = createUser("userb", "userb@test.com", "User B", "pix-b");
        userC = createUser("userc", "userc@test.com", "User C", "pix-c");

        group = new Group();
        group.setName("Grupo de Teste");
        group.setEventDate(Instant.now());
        group.setInviteCode(UUID.randomUUID().toString().substring(0, 8));
        group.setExternalId(UUID.randomUUID());
        group = groupRepository.save(group);

        memberA = createMember(group, userA, GroupRole.ADMIN);
        memberB = createMember(group, userB, GroupRole.MEMBER);
        memberC = createMember(group, userC, GroupRole.MEMBER);

        group.getMembers().add(memberA);
        group.getMembers().add(memberB);
        group.getMembers().add(memberC);
        group = groupRepository.save(group);

        memberA = group.getMembers().get(0);
        memberB = group.getMembers().get(1);
        memberC = group.getMembers().get(2);
    }

    private User createUser(String username, String email, String name, String pixKey) {
        User u = new User();
        u.setExternalId(UUID.randomUUID());
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("password");
        u.setDisplayName(name);
        u.setBirthDate(LocalDate.of(1990, 1, 1));

        PixInfo pix = PixInfo.builder()
            .pixKey(pixKey)
            .merchantName(name)
            .merchantCity("São Paulo")
            .build();
        pix.setExternalId(UUID.randomUUID());
        u.setPixInfo(pix);

        return userRepository.save(u);
    }

    private GroupMember createMember(Group g, User u, GroupRole role) {
        GroupMember m = new GroupMember();
        m.setGroup(g);
        m.setUser(u);
        m.setRole(role);
        m.setExternalId(UUID.randomUUID());
        return m;
    }

    @Test
    void testCreateAutoWithBillingOrchestrator() {
        ExpenseContributionRequest contA = new ExpenseContributionRequest(90.0, userA.getExternalId());
        ExpenseContributionRequest contB = new ExpenseContributionRequest(0.0, userB.getExternalId());
        ExpenseContributionRequest contC = new ExpenseContributionRequest(0.0, userC.getExternalId());

        ExpenseAutoCreationRequest request = new ExpenseAutoCreationRequest(
            "Almoço",
            90.0,
            List.of(contA, contB, contC)
        );

        Expense expense = expenseService.createAuto(userA, group.getInviteCode(), request);

        assertNotNull(expense.getId());
        assertEquals(9000L, expense.getTotalValue());
        assertEquals(3, expense.getExpenseContributions().size());
        assertEquals(2, expense.getExpenseDistributions().size());

        expense.getExpenseDistributions().forEach(dist -> {
            assertEquals(3000L, dist.getValue());
            assertEquals(DebtStatus.PENDING, dist.getStatus());
            assertEquals(memberA.getId(), dist.getCreditor().getId());
            assertTrue(dist.getDebtor().getId().equals(memberB.getId()) || dist.getDebtor().getId().equals(memberC.getId()));
        });
    }

    @Test
    void testCreateManualPreserved() {
        ExpenseContributionRequest contA = new ExpenseContributionRequest(100.0, userA.getExternalId());
        
        ExpenseDistributionRequest distB = new ExpenseDistributionRequest(60.0, userB.getExternalId(), userA.getExternalId());
        ExpenseDistributionRequest distC = new ExpenseDistributionRequest(40.0, userC.getExternalId(), userA.getExternalId());

        ExpenseManualCreationRequest request = new ExpenseManualCreationRequest(
            "Jantar Específico",
            100.0,
            List.of(contA),
            List.of(distB, distC)
        );

        Expense expense = expenseService.createManual(userA, group.getInviteCode(), request);

        assertNotNull(expense.getId());
        assertEquals(10000L, expense.getTotalValue());
        assertEquals(1, expense.getExpenseContributions().size());
        assertEquals(2, expense.getExpenseDistributions().size());

        boolean foundB = expense.getExpenseDistributions().stream()
            .anyMatch(d -> d.getDebtor().getId().equals(memberB.getId()) && d.getValue().equals(6000L));
        boolean foundC = expense.getExpenseDistributions().stream()
            .anyMatch(d -> d.getDebtor().getId().equals(memberC.getId()) && d.getValue().equals(4000L));

        assertTrue(foundB, "Deve encontrar a dívida de 60.00 do membro B");
        assertTrue(foundC, "Deve encontrar a dívida de 40.00 do membro C");
    }

    @Test
    void testDebtStatusTransitions() {
        ExpenseContributionRequest contA = new ExpenseContributionRequest(100.0, userA.getExternalId());
        ExpenseDistributionRequest distB = new ExpenseDistributionRequest(100.0, userB.getExternalId(), userA.getExternalId());

        ExpenseManualCreationRequest request = new ExpenseManualCreationRequest(
            "Jantar",
            100.0,
            List.of(contA),
            List.of(distB)
        );

        Expense expense = expenseService.createManual(userA, group.getInviteCode(), request);
        com.role.net.gogather.entity.ExpenseDistribution dist = expense.getExpenseDistributions().iterator().next();

        assertEquals(DebtStatus.PENDING, dist.getStatus());

        expenseService.markAsPaid(userB.getId(), dist.getExternalId());
        dist = expenseService.findExpenseDistributionByExternalId(dist.getExternalId());
        assertEquals(DebtStatus.AWAITING_CONFIRMATION, dist.getStatus());

        expenseService.confirmReceipt(userA.getId(), dist.getExternalId());
        dist = expenseService.findExpenseDistributionByExternalId(dist.getExternalId());
        assertEquals(DebtStatus.PAID, dist.getStatus());
    }
}
