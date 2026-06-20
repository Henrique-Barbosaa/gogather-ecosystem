package gogather.framework.billing.dto;

import gogather.framework.core.Participant;

public record Contribution(Participant participant, Long amountInCents) {}
