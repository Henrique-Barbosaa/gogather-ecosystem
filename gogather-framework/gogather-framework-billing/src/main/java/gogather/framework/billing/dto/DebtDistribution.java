package gogather.framework.billing.dto;

import gogather.framework.core.Participant;

public record DebtDistribution(Participant debtor, Participant creditor, Long amountInCents, DebtStatus status) {
    public DebtDistribution(Participant debtor, Participant creditor, Long amountInCents) {
        this(debtor, creditor, amountInCents, DebtStatus.PENDING);
    }
}
