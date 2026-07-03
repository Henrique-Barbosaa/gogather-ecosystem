package com.role.net.roomiesapp;

import com.role.net.roomiesapp.dto.billing.BillResponse;
import com.role.net.roomiesapp.dto.billing.CreateBillRequest;
import com.role.net.roomiesapp.dto.pix.PixInfoResponse;
import com.role.net.roomiesapp.dto.pix.RegisterPixKeyRequest;
import com.role.net.roomiesapp.entity.*;
import com.role.net.roomiesapp.repository.HouseBillRepository;
import com.role.net.roomiesapp.repository.HouseDebtRepository;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.UserRepository;
import com.role.net.roomiesapp.service.RoomiesBillingService;
import com.role.net.roomiesapp.service.UserPixService;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.group.jpa.domain.GroupRole;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class RoomiesGroupBillingIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomiesGroupRepository groupRepository;

    @Autowired
    private UserPixService userPixService;

    @Autowired
    private RoomiesBillingService billingService;

    @Autowired
    private HouseDebtRepository debtRepository;

    @Autowired
    private GroupMembershipOrchestrator groupMembershipOrchestrator;

    @Test
    public void testGroupCreationMembershipAndBillingFlow() {
        // 1. Create two users (Alice and Bob)
        User alice = new User();
        alice.setUsername("alice_roomie");
        alice.setEmail("alice@roomies.com");
        alice.setPassword("pass123");
        alice.setDisplayName("Alice Roomie");
        alice = userRepository.save(alice);

        User bob = new User();
        bob.setUsername("bob_roomie");
        bob.setEmail("bob@roomies.com");
        bob.setPassword("pass123");
        bob.setDisplayName("Bob Roomie");
        bob = userRepository.save(bob);

        // 2. Register Pix key for Alice (contributor/creditor)
        userPixService.registerOrUpdatePix(alice, new RegisterPixKeyRequest("alice@roomiespix.com", "Alice Souza", "Sao Paulo"));
        PixInfoResponse pixInfo = userPixService.getPixInfo(alice);
        assertNotNull(pixInfo);
        assertEquals("alice@roomiespix.com", pixInfo.pixKey());

        // 3. Create House Group with Alice as ADMIN
        Group household = new Group();
        household.setName("República Teste");
        household.setDescription("Teste de Rateio de Contas");
        household.setAddress("Rua das Flores, 123");
        household.setMonthlyRentCents(200000L); // 2000 BRL
        household.setMaxOccupants(4);
        household.setInviteCode("ROOM2026");
        household = groupRepository.save(household);

        household.addMember(alice, GroupRole.ADMIN);
        household = groupRepository.save(household);

        // 4. Use framework GroupMembershipOrchestrator to invite Bob to the household
        groupMembershipOrchestrator.inviteUserToGroup("ROOM2026", bob.getId().toString(), alice.getId().toString());

        // Verify Bob is now a member of the group
        Group updatedHousehold = groupRepository.findByInviteCode("ROOM2026").orElseThrow();
        assertEquals(2, updatedHousehold.getMembers().size());
        assertTrue(updatedHousehold.hasMember(bob.getId().toString()));

        // 5. Create House Bill (Alice paid 200.00 BRL for internet bill)
        CreateBillRequest billRequest = new CreateBillRequest(
                "Conta de Internet",
                "Fibra Óptica 500Mbps",
                20000L,
                BillType.NORMAL,
                RecurrenceInterval.NONE,
                null,
                LocalDate.now().plusDays(10),
                alice.getExternalId(),
                List.of() // Split among all members (Alice and Bob)
        );

        BillResponse billResponse = billingService.createBill(updatedHousehold.getExternalId(), billRequest, alice);
        assertNotNull(billResponse);
        assertEquals(20000L, billResponse.totalCents());

        // 6. Verify that framework generated debt: Bob owes Alice 100.00 BRL (10000 cents)
        List<HouseDebt> debts = debtRepository.findAll();
        assertFalse(debts.isEmpty());
        
        final Long aliceId = alice.getId();
        final Long bobId = bob.getId();
        HouseDebt debt = debts.stream()
                .filter(d -> d.getDebtor().getId().equals(bobId) && d.getCreditor().getId().equals(aliceId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Debt from Bob to Alice not found"));

        assertEquals(10000L, debt.getAmountInCents());
        assertEquals(DebtStatus.PENDING, debt.getStatus());
    }
}
