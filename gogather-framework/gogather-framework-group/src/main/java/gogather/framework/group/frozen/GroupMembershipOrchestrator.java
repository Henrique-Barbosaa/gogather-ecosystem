package gogather.framework.group.frozen;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupDataProvider;
import gogather.framework.group.core.GroupInviteValidationStrategy;

public class GroupMembershipOrchestrator {
    private final GroupDataProvider dataProvider;
    private final GroupInviteValidationStrategy validationStrategy;

    public GroupMembershipOrchestrator(
        GroupDataProvider dataProvider,
        GroupInviteValidationStrategy validationStrategy
    ) {
        this.dataProvider = dataProvider;
        this.validationStrategy = validationStrategy;
    }

    public void inviteUserToGroup(String groupId, String inviteeId, String inviterId) {

        Group group = dataProvider.findGroup(groupId);
        Participant invitee = dataProvider.findMember(inviteeId);
        Participant inviter = dataProvider.findMember(inviterId);

        if (group.hasMember(invitee.getIdentifier())) {
            throw new IllegalStateException("Esse usuário já está no rolê ou já foi convidado.");
        }

        validationStrategy.validate(group, inviter, invitee);

        group.addPendindParticipant(invitee, inviter);

        dataProvider.save(group);
    }
}
