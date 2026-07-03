package com.role.net.roomiesapp.service.provider;

import com.role.net.roomiesapp.entity.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import gogather.framework.group.exception.UserAlreadyInGroupException;
import org.springframework.stereotype.Component;

@Component
public class AppGroupValidationStrategy implements GroupInviteValidationStrategy {

    @Override
    public void validate(gogather.framework.core.Group group, Participant inviter, Participant invitee) {
        Group household = (Group) group;

        int ocupantesAtuais = household.getMembers() != null ? household.getMembers().size() : 0;
        Integer limite = household.getMaxOccupants();

        if (limite != null && ocupantesAtuais >= limite) {
            throw new UserAlreadyInGroupException(
                "Esta república já atingiu o limite de " + limite + " moradores.");
        }
    }
}