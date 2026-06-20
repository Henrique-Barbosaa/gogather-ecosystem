package gogather.framework.billing.dto;

import gogather.framework.billing.core.Participant;

public record Contribution(Participant participant, Long amountInCents) {}
