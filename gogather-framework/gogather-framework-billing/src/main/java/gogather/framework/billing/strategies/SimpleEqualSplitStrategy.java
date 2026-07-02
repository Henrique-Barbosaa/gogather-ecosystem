package gogather.framework.billing.strategies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gogather.framework.billing.core.AbstractExpenseSplitter;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.core.Participant;

/**
 * Implementação padrão fornecida pelo framework para divisão igualitária de despesas.
 * Estende o Template Method (AbstractExpenseSplitter), sobrescrevendo apenas o método gancho
 * de cálculo das cotas individuais devidas por cada participante, delegando todo o restante
 * (validação e conciliação de dívidas) para a estrutura invariante da classe mãe.
 */
public class SimpleEqualSplitStrategy extends AbstractExpenseSplitter {

    @Override
    protected Map<String, Long> calculateOwedAmounts(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    ) {
        int membersAmount = participants.size();
        long individualQuota = totalCents / membersAmount;
        int remainingCents = Math.toIntExact(totalCents % membersAmount);

        Map<String, Long> owedAmounts = new HashMap<>();

        for (Participant p : participants) {
            long memberQuota = individualQuota;

            if (remainingCents > 0) {
                memberQuota++;
                remainingCents--;
            }

            owedAmounts.put(p.getIdentifier(), memberQuota);
        }

        return owedAmounts;
    }
}
