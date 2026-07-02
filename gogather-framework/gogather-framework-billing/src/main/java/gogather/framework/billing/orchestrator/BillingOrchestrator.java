package gogather.framework.billing.orchestrator;

import java.util.List;
import gogather.framework.billing.core.AbstractExpenseSplitter;
import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.core.Participant;

/**
 * Orquestrador de Billing (Frozen Spot do framework) que implementa o Princípio de Hollywood
 * ("Don't call us, we'll call you"). O orquestrador assume o controle do fluxo de execução do rateio,
 * convocando o provedor de dados (Hot Spot implementado pela aplicação) para buscar as informações
 * da despesa, aplicando o algoritmo do Template Method (AbstractExpenseSplitter) e convocando
 * novamente o provedor para persistir as distribuições de dívida geradas.
 */
public class BillingOrchestrator {
    private final BillingDataProvider dataProvider;
    private final AbstractExpenseSplitter expenseSplitter;

    public BillingOrchestrator(BillingDataProvider dataProvider, AbstractExpenseSplitter expenseSplitter) {
        this.dataProvider = dataProvider;
        this.expenseSplitter = expenseSplitter;
    }

    /**
     * Executa o fluxo completo de conciliação e rateio da despesa.
     *
     * @param expenseId Identificador da despesa no sistema consumidor.
     * @return Lista das transferências financeiras calculadas e salvas.
     */
    public List<DebtDistribution> settleExpense(String expenseId) {
        Long totalCents = dataProvider.getTotalCents(expenseId);
        List<Participant> participants = dataProvider.getParticipants(expenseId);
        List<Contribution> contributions = dataProvider.getContributions(expenseId);

        List<DebtDistribution> distributions = expenseSplitter.calculateSplit(totalCents, participants, contributions);

        dataProvider.saveDistributions(expenseId, distributions);

        return distributions;
    }
}
