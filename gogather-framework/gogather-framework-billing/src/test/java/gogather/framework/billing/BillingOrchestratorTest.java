package gogather.framework.billing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import gogather.framework.billing.core.AbstractExpenseSplitter;
import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.billing.orchestrator.BillingOrchestrator;
import gogather.framework.billing.strategies.SimpleEqualSplitStrategy;
import gogather.framework.core.Participant;

public class BillingOrchestratorTest {

    private static class MockParticipant implements Participant {
        private final String id;
        public MockParticipant(String id) { this.id = id; }
        @Override public String getIdentifier() { return id; }
    }

    private static class MockBillingDataProvider implements BillingDataProvider {
        private final Long totalCents;
        private final List<Participant> participants;
        private final List<Contribution> contributions;
        public List<DebtDistribution> savedDistributions = new ArrayList<>();

        public MockBillingDataProvider(Long totalCents, List<Participant> participants, List<Contribution> contributions) {
            this.totalCents = totalCents;
            this.participants = participants;
            this.contributions = contributions;
        }

        @Override public Long getTotalCents(String expenseId) { return totalCents; }
        @Override public List<Participant> getParticipants(String expenseId) { return participants; }
        @Override public List<Contribution> getContributions(String expenseId) { return contributions; }
        @Override public void saveDistributions(String expenseId, List<DebtDistribution> distributions) {
            this.savedDistributions = distributions;
        }
    }

    @Test
    public void testTemplateMethodWithEqualSplit() {
        Participant alice = new MockParticipant("alice");
        Participant bob = new MockParticipant("bob");
        Participant charlie = new MockParticipant("charlie");

        List<Participant> participants = List.of(alice, bob, charlie);
        List<Contribution> contributions = List.of(
            new Contribution(alice, 300L),
            new Contribution(bob, 0L),
            new Contribution(charlie, 0L)
        );

        AbstractExpenseSplitter splitter = new SimpleEqualSplitStrategy();
        List<DebtDistribution> distributions = splitter.calculateSplit(300L, participants, contributions);

        assertEquals(2, distributions.size());
        for (DebtDistribution dist : distributions) {
            assertEquals("alice", dist.creditor().getIdentifier());
            assertTrue(dist.debtor().getIdentifier().equals("bob") || dist.debtor().getIdentifier().equals("charlie"));
            assertEquals(100L, dist.amountInCents());
        }
    }

    @Test
    public void testTemplateMethodWithCustomProportionalSplit() {
        Participant alice = new MockParticipant("alice");
        Participant bob = new MockParticipant("bob");

        List<Participant> participants = List.of(alice, bob);
        List<Contribution> contributions = List.of(
            new Contribution(alice, 1000L),
            new Contribution(bob, 0L)
        );

        // Subclasse customizada do Template Method: 70% para alice, 30% para bob
        AbstractExpenseSplitter customSplitter = new AbstractExpenseSplitter() {
            @Override
            protected Map<String, Long> calculateOwedAmounts(Long totalCents, List<Participant> participants, List<Contribution> contributions) {
                Map<String, Long> owed = new HashMap<>();
                owed.put("alice", 700L);
                owed.put("bob", 300L);
                return owed;
            }
        };

        List<DebtDistribution> distributions = customSplitter.calculateSplit(1000L, participants, contributions);

        assertEquals(1, distributions.size());
        assertEquals("alice", distributions.get(0).creditor().getIdentifier());
        assertEquals("bob", distributions.get(0).debtor().getIdentifier());
        assertEquals(300L, distributions.get(0).amountInCents());
    }

    @Test
    public void testHollywoodPrincipleWithOrchestrator() {
        Participant alice = new MockParticipant("alice");
        Participant bob = new MockParticipant("bob");

        List<Participant> participants = List.of(alice, bob);
        List<Contribution> contributions = List.of(
            new Contribution(alice, 500L),
            new Contribution(bob, 0L)
        );

        MockBillingDataProvider dataProvider = new MockBillingDataProvider(500L, participants, contributions);
        AbstractExpenseSplitter splitter = new SimpleEqualSplitStrategy();
        BillingOrchestrator orchestrator = new BillingOrchestrator(dataProvider, splitter);

        // O Orquestrador comanda o fluxo (Don't call us, we'll call you)
        List<DebtDistribution> result = orchestrator.settleExpense("expense-123");

        assertEquals(1, result.size());
        assertEquals(1, dataProvider.savedDistributions.size());
        assertEquals(250L, dataProvider.savedDistributions.get(0).amountInCents());
        assertEquals("bob", dataProvider.savedDistributions.get(0).debtor().getIdentifier());
        assertEquals("alice", dataProvider.savedDistributions.get(0).creditor().getIdentifier());
    }
}
