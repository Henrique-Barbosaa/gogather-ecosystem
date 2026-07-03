package com.role.net.tripmaker.provider;

import com.role.net.tripmaker.entity.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import gogather.framework.group.exception.UserAlreadyInGroupException;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AppGroupValidationStrategy implements GroupInviteValidationStrategy {

    @Override
    public void validate(gogather.framework.core.Group group, Participant inviter, Participant invitee) {
        Group trip = (Group) group;

        if (trip.getStartDate() != null && trip.getStartDate().isBefore(LocalDate.now())) {
            throw new UserAlreadyInGroupException(
                "Esta viagem já começou e não aceita novos viajantes.");
        }

        int viajantesAtuais = trip.getMembers() != null ? trip.getMembers().size() : 0;
        Integer limite = trip.getMaxTravelers();
        if (limite != null && viajantesAtuais >= limite) {
            throw new UserAlreadyInGroupException(
                "Esta viagem já atingiu o limite de " + limite + " viajantes.");
        }
    }
}