package gogather.framework.billing.core;

import java.util.List;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.core.Participant;

/**
 * Interface de gancho (Hot Spot) do Princípio de Hollywood para o módulo de Billing.
 * A aplicação consumidora implementa esta interface para fornecer os dados da despesa
 * e persistir o resultado do rateio quando convocada pelo orquestrador do framework.
 */
public interface BillingDataProvider {
    Long getTotalCents(String expenseId);
    List<Participant> getParticipants(String expenseId);
    List<Contribution> getContributions(String expenseId);
    void saveDistributions(String expenseId, List<DebtDistribution> distributions);
}
