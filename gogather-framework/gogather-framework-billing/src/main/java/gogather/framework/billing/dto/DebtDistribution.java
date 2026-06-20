package gogather.framework.billing.dto;

import gogather.framework.billing.core.Participant;

public record DebtDistribution(Participant debtor, Participant creditor, Long amountInCents) {}
