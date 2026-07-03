package gogather.framework.billing.dto;

import gogather.framework.core.Participant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DebtDistributionTest {

    private static class MockParticipant implements Participant {
        private final String id;
        public MockParticipant(String id) { this.id = id; }
        @Override public String getIdentifier() { return id; }
    }

    @Test
    public void testDebtDistributionWithCustomStatus() {
        Participant debtor = new MockParticipant("user-1");
        Participant creditor = new MockParticipant("user-2");

        DebtDistribution dist = new DebtDistribution(debtor, creditor, 2500L, DebtStatus.AWAITING_CONFIRMATION);

        assertEquals("user-1", dist.debtor().getIdentifier());
        assertEquals("user-2", dist.creditor().getIdentifier());
        assertEquals(2500L, dist.amountInCents());
        assertEquals(DebtStatus.AWAITING_CONFIRMATION, dist.status());
    }

    @Test
    public void testDebtDistributionDefaultStatusIsPending() {
        Participant debtor = new MockParticipant("user-1");
        Participant creditor = new MockParticipant("user-2");

        DebtDistribution dist = new DebtDistribution(debtor, creditor, 1000L);

        assertEquals("user-1", dist.debtor().getIdentifier());
        assertEquals("user-2", dist.creditor().getIdentifier());
        assertEquals(1000L, dist.amountInCents());
        assertEquals(DebtStatus.PENDING, dist.status());
    }
}
