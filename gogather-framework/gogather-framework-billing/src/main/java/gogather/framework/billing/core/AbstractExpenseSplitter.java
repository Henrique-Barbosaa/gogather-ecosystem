package gogather.framework.billing.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.billing.dto.ParticipantValue;
import gogather.framework.core.Participant;

/**
 * Classe abstrata que implementa o padrão Template Method para rateio de despesas.
 * Define o algoritmo principal em {@link #calculateSplit(Long, List, List)},
 * mantendo as etapas invariantes (validação, soma de contribuições, categorização
 * de saldos e algoritmo de conciliação) como comportamento padrão, e delegando
 * apenas o cálculo das cotas devidas (gancho) para as subclasses.
 */
public abstract class AbstractExpenseSplitter {

    /**
     * Template Method: define o esqueleto do algoritmo de rateio e liquidação.
     * O método é final para garantir que a estrutura e sequência do algoritmo não
     * sejam corrompidas por implementações concretas.
     *
     * @param totalCents O valor total da despesa em centavos.
     * @param participants Lista de todos os participantes envolvidos no rateio.
     * @param contributions Lista de contribuições de quem pagou o que.
     * @return Lista de transferências (DebtDistribution) necessárias para quitar a conta.
     */
    public final List<DebtDistribution> calculateSplit(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    ) {
        validate(totalCents, participants, contributions);

        Map<String, Long> paidAmounts = calculatePaidAmounts(contributions);
        Map<String, Long> owedAmounts = calculateOwedAmounts(totalCents, participants, contributions);

        List<ParticipantValue> receivers = new ArrayList<>();
        List<ParticipantValue> payers = new ArrayList<>();
        categorizeParticipants(participants, paidAmounts, owedAmounts, receivers, payers);

        return settleDebts(receivers, payers);
    }

    /**
     * Etapa padrão de validação dos dados de entrada.
     * Garante que o total de contribuições seja igual ao valor total da despesa.
     */
    protected void validate(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    ) {
        if (totalCents == null || totalCents < 0) {
            throw new IllegalArgumentException("O valor total da despesa deve ser um número positivo.");
        }
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("A lista de participantes não pode estar vazia.");
        }
        if (contributions == null) {
            throw new IllegalArgumentException("A lista de contribuições não pode ser nula.");
        }
        long totalContributed = contributions.stream().mapToLong(Contribution::amountInCents).sum();
        if (totalContributed != totalCents) {
            throw new IllegalArgumentException("A soma das contribuições não bate com o valor total.");
        }
    }

    /**
     * Etapa padrão que agrupa e calcula o valor total pago por cada participante.
     */
    protected Map<String, Long> calculatePaidAmounts(List<Contribution> contributions) {
        return contributions.stream()
            .collect(Collectors.toMap(
                c -> c.participant().getIdentifier(),
                Contribution::amountInCents,
                Long::sum
            ));
    }

    /**
     * Hook Method (Gancho) abstrato onde cada estratégia específica define quanto cada
     * participante deve pagar (sua cota na despesa).
     *
     * @param totalCents Valor total da despesa.
     * @param participants Lista de participantes envolvidos.
     * @param contributions Contribuições realizadas.
     * @return Mapeamento de identificador do participante para o valor devido em centavos.
     */
    protected abstract Map<String, Long> calculateOwedAmounts(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    );

    /**
     * Etapa padrão de categorização dos participantes em credores (receivers)
     * e devedores (payers) baseado no saldo líquido (pago - devido).
     */
    protected void categorizeParticipants(
        List<Participant> participants,
        Map<String, Long> paidAmounts,
        Map<String, Long> owedAmounts,
        List<ParticipantValue> receivers,
        List<ParticipantValue> payers
    ) {
        for (Participant p : participants) {
            long paidValue = paidAmounts.getOrDefault(p.getIdentifier(), 0L);
            long owedValue = owedAmounts.getOrDefault(p.getIdentifier(), 0L);
            long balance = paidValue - owedValue;

            if (balance > 0) {
                receivers.add(new ParticipantValue(p, balance));
            } else if (balance < 0) {
                payers.add(new ParticipantValue(p, Math.abs(balance)));
            }
        }
    }

    /**
     * Etapa padrão com o algoritmo ganancioso (greedy) de liquidação e conciliação
     * das dívidas, cruzando credores com devedores.
     */
    protected List<DebtDistribution> settleDebts(
        List<ParticipantValue> receivers,
        List<ParticipantValue> payers
    ) {
        List<DebtDistribution> distributions = new ArrayList<>();
        int payersIndex = 0;
        int receiversIndex = 0;

        while (receiversIndex < receivers.size() && payersIndex < payers.size()) {
            long receiverRemaining = receivers.get(receiversIndex).cents();
            long payerRemaining = payers.get(payersIndex).cents();

            Participant payerParticipant = payers.get(payersIndex).participant();
            Participant receiverParticipant = receivers.get(receiversIndex).participant();

            if (payerRemaining <= receiverRemaining) {
                distributions.add(new DebtDistribution(payerParticipant, receiverParticipant, payerRemaining));

                if (payerRemaining == receiverRemaining) {
                    receiversIndex++;
                    payersIndex++;
                } else {
                    payersIndex++;
                    receivers.set(
                        receiversIndex,
                        new ParticipantValue(receiverParticipant, receiverRemaining - payerRemaining)
                    );
                }
            } else {
                distributions.add(new DebtDistribution(payerParticipant, receiverParticipant, receiverRemaining));

                receiversIndex++;
                payers.set(
                    payersIndex,
                    new ParticipantValue(payerParticipant, payerRemaining - receiverRemaining)
                );
            }
        }

        return distributions;
    }
}
