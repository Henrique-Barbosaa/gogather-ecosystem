package gogather.framework.billing.dto;

import gogather.framework.core.Participant;

public record ParticipantValue(Participant participant, Long cents) {}
