package gogather.framework.group.orchestrator;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupDataProvider;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import gogather.framework.group.exception.UserAlreadyInGroupException;

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
            throw new UserAlreadyInGroupException("User is already a member of the group or has a pending invitation.");
        }

        validationStrategy.validate(group, inviter, invitee);

        group.addPendindParticipant(invitee, inviter);

        dataProvider.save(group);
    }
}
