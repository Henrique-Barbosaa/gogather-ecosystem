package com.role.net.gogather.service.provider;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import org.springframework.stereotype.Component;

@Component
public class AppGroupValidationStrategy implements GroupInviteValidationStrategy {

    @Override
    public void validate(Group group, Participant inviter, Participant invitee) {
        com.role.net.gogather.entity.Group tripGroup = (com.role.net.gogather.entity.Group) group;
        
        if (tripGroup.getMembers() != null && tripGroup.getMembers().size() >= 20) {
            throw new RuntimeException("Este grupo de viagem já atingiu o limite máximo de participantes.");
        }
    }
}