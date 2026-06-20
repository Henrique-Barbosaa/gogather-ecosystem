package gogather.framework.billing.dto;

import gogather.framework.billing.core.Participant;

public record ParticipantValue(Participant participant, Long cents) {}
