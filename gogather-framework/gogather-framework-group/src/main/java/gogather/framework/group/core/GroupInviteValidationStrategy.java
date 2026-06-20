package gogather.framework.group.core;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;

public interface GroupInviteValidationStrategy {
    void validate(Group group, Participant inviter, Participant invitee);
}
