package gogather.framework.group.jpa.service;

import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.domain.BaseUser;
import gogather.framework.group.jpa.domain.GroupMember;
import gogather.framework.group.jpa.domain.GroupRole;
import gogather.framework.group.jpa.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service("fwGroupService")
public class GroupService {

    private final GroupRepository groupRepository; 

    public GroupService(@Qualifier("fwGroupRepository") GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public <T extends BaseGroup> T createGroup(T group, BaseUser creator) {
        group.setInviteCode(generateUniqueInviteCode());
        
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(creator);
        member.setRole(GroupRole.ADMIN);
        
        group.getMembers().add(member);

        return groupRepository.save(group);
    }

    private String generateUniqueInviteCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = sb.toString();
        } while (groupRepository.existsByInviteCode(code));
        
        return code;
    }
}