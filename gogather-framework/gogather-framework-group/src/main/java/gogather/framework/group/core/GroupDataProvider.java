package gogather.framework.group.core;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;

public interface GroupDataProvider {
    Group findGroup(String groupId);
    Participant findMember(String memberId);
    void save(Group group);
}
