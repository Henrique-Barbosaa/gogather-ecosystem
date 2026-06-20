package gogather.framework.billing.dto;

import gogather.framework.core.Participant;

public record DebtDistribution(Participant debtor, Participant creditor, Long amountInCents) {}
