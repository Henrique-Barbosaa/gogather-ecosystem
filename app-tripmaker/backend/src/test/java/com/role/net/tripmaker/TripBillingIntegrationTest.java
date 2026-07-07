package com.role.net.tripmaker;

import com.role.net.tripmaker.dto.expense.ContributionRequest;
import com.role.net.tripmaker.dto.expense.CreateExpenseRequest;
import com.role.net.tripmaker.dto.expense.PixCodeResponse;
import com.role.net.tripmaker.dto.expense.TripDebtResponse;
import com.role.net.tripmaker.dto.expense.TripExpenseResponse;
import com.role.net.tripmaker.dto.user.RegisterPixKeyRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;
import com.role.net.tripmaker.service.UserService;
import com.role.net.tripmaker.service.billing.TripBillingService;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.group.jpa.domain.GroupRole;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class TripBillingIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripGroupRepository groupRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TripBillingService billingService;

    @Test
    public void testCompleteExpenseAndBillingFlow() {
        // 1. Create two users
        User alice = new User();
        alice.setUsername("alice_test");
        alice.setEmail("alice@test.com");
        alice.setPassword("pass");
        alice.setDisplayName("Alice");
        alice = userRepository.save(alice);

        User bob = new User();
        bob.setUsername("bob_test");
        bob.setEmail("bob@test.com");
        bob.setPassword("pass");
        bob.setDisplayName("Bob");
        bob = userRepository.save(bob);

        // 2. Register Pix key for Alice
        userService.registerPix(alice.getId(), new RegisterPixKeyRequest("alice@pix.com", "EMAIL", "Alice Souza", "Sao Paulo"));
        alice = userRepository.findById(alice.getId()).orElseThrow();
        assertNotNull(alice.getPixInfo());
        assertEquals("alice@pix.com", alice.getPixInfo().getPixKey());

        // 3. Create Trip Group with Alice and Bob
        Group trip = new Group();
        trip.setName("Viagem de Teste");
        trip.setDescription("Teste de Rateio");
        trip.setDestination("Praia");
        trip.setMaxTravelers(10);
        trip.setInviteCode("TRIP2026");
        trip = groupRepository.save(trip);

        trip.addMember(alice, GroupRole.ADMIN);
        trip.addMember(bob, GroupRole.MEMBER);
        trip = groupRepository.save(trip);

        // 4. Alice pays 100.00 BRL (10000 cents) for dinner
        CreateExpenseRequest request = new CreateExpenseRequest(
            "Jantar",
            LocalDate.now(),
            null,
            List.of(new ContributionRequest(alice.getId(), 10000L)),
            List.of() // Split among all members (Alice and Bob)
        );

        TripExpenseResponse expenseResponse = billingService.createExpense(trip.getId(), request, alice);
        assertNotNull(expenseResponse);
        assertEquals(10000L, expenseResponse.totalCents());

        // 5. Verify that framework generated debt: Bob owes Alice 5000 cents (50 BRL)
        List<TripDebtResponse> debts = billingService.getTripDebts(trip.getId(), alice);
        assertEquals(1, debts.size());
        TripDebtResponse debt = debts.get(0);
        assertEquals(bob.getId(), debt.debtorId());
        assertEquals(alice.getId(), debt.creditorId());
        assertEquals(5000L, debt.amountInCents());
        assertEquals(DebtStatus.PENDING, debt.status());

        // 6. Test status transition: Bob reports payment (AWAITING_CONFIRMATION)
        TripDebtResponse updatedDebt = billingService.updateDebtStatus(trip.getId(), debt.id(), DebtStatus.AWAITING_CONFIRMATION, bob);
        assertEquals(DebtStatus.AWAITING_CONFIRMATION, updatedDebt.status());

        // 7. Alice denies payment confirmation, sending status back to PENDING
        updatedDebt = billingService.updateDebtStatus(trip.getId(), debt.id(), DebtStatus.PENDING, alice);
        assertEquals(DebtStatus.PENDING, updatedDebt.status());

        // 8. Bob reports payment again (AWAITING_CONFIRMATION) and Alice confirms payment receipt (PAID)
        updatedDebt = billingService.updateDebtStatus(trip.getId(), debt.id(), DebtStatus.AWAITING_CONFIRMATION, bob);
        updatedDebt = billingService.updateDebtStatus(trip.getId(), debt.id(), DebtStatus.PAID, alice);
        assertEquals(DebtStatus.PAID, updatedDebt.status());

        // 8. Test Pix code generation for Bob to pay Alice
        PixCodeResponse pixResponse = billingService.generatePixCodeForDebt(trip.getId(), debt.id(), bob);
        assertNotNull(pixResponse);
        assertEquals("alice@pix.com", pixResponse.pixKey());
        assertEquals(5000L, pixResponse.amountInCents());
        assertTrue(pixResponse.pixCopyAndPaste().startsWith("000201"));
        assertTrue(pixResponse.pixCopyAndPaste().contains("6304"));
    }
}
