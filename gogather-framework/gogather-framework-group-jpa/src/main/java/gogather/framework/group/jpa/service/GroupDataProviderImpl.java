package gogather.framework.group.jpa.service;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupDataProvider;
import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.repository.GroupRepository;
import gogather.framework.group.jpa.repository.BaseUserRepository;
import org.springframework.stereotype.Component;

@Component
public class GroupDataProviderImpl implements GroupDataProvider {

    private final GroupRepository groupRepository;
    private final BaseUserRepository userRepository;

    public GroupDataProviderImpl(GroupRepository groupRepository, BaseUserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Group findGroup(String groupId) {
        // O frontend vai mandar o InviteCode pela URL
        return groupRepository.findByInviteCode(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado pelo código: " + groupId));
    }

    @Override
    public Participant findMember(String memberId) {
        if (memberId == null) return null;
        return userRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    @Override
    public void save(Group group) {
        if (group instanceof BaseGroup) {
            //usamos CascadeType.ALL no BaseGroup, ao salvar o grupo, o novo membro é salvo junto
            groupRepository.save((BaseGroup) group);
        }
    }
}