package gogather.framework.billing.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gogather.framework.billing.core.ExpenseSplitStrategy;
import gogather.framework.billing.dto.Contribution;
import gogather.framework.billing.dto.DebtDistribution;
import gogather.framework.billing.dto.ParticipantValue;
import gogather.framework.core.Participant;

public class SimpleEqualSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<DebtDistribution> calculateSplit(
        Long totalCents,
        List<Participant> participants,
        List<Contribution> contributions
    ) {
        long totalContributed = contributions.stream().mapToLong(Contribution::amountInCents).sum();
        if (totalContributed != totalCents) {
            throw new IllegalArgumentException("A soma das contribuições não bate com o valor total.");
        }

        int membersAmount = participants.size();
        long individualQuota = totalCents / membersAmount;
        int remainingCents = Math.toIntExact(totalCents % membersAmount);

        Map<String, Long> paidAmount = contributions.stream()
            .collect(Collectors.toMap(
                c -> c.participant().getIdentifier(),
                Contribution::amountInCents,
                Long::sum
            ));

        List<ParticipantValue> receivers = new ArrayList<>();
        List<ParticipantValue> payers = new ArrayList<>();

        for(Participant p : participants) {
            long memberQuota = individualQuota;

            if(remainingCents > 0) {
                memberQuota++;
                remainingCents--;
            }

            long paidValue = paidAmount.getOrDefault(p.getIdentifier(), 0L);
            long balance = paidValue - memberQuota;

            if(balance > 0) {
                receivers.add(new ParticipantValue(p, Math.abs(balance)));
            } else if(balance < 0) {
                payers.add(new ParticipantValue(p, Math.abs(balance)));
            }
        }

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
