package gogather.framework.billing.core;

import java.util.List;

import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;

public interface ExpenseSplitStrategy {

    /**
    * Calcula quem deve quem
    * @param totalCents O valor total da despesa.
    * @param participants Todos os envolvidos no Rateio.
    * @param contributions Quem pagou o que.
    * @return Lista de transferências necessárias para quitar a conta.
 */
    List<DebtDistribution> calculateSplit(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    );
}
