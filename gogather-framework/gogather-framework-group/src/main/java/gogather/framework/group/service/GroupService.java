package gogather.framework.group.service;

import gogather.framework.group.domain.BaseGroup;
import gogather.framework.group.domain.BaseUser;
import gogather.framework.group.domain.GroupMember;
import gogather.framework.group.domain.GroupRole;
import gogather.framework.group.exception.UserAlreadyInGroupException;
import gogather.framework.group.repository.BaseGroupRepository;
import gogather.framework.group.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class GroupService {

    private final BaseGroupRepository<BaseGroup> groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupService(BaseGroupRepository<BaseGroup> groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // O uso de <T extends BaseGroup> garante que se o App mandar um "AcademicGroup", ele devolve um "AcademicGroup"
    @Transactional
    public <T extends BaseGroup> T createGroup(T group, BaseUser creator) {
        // 1. Gera código único alfanumérico
        group.setInviteCode(generateUniqueInviteCode());
        
        // 2. Salva o grupo (seja ele um grupo de viagem, estudo ou república)
        T savedGroup = groupRepository.save(group);

        // 3. Associa o criador como ADMIN absoluto
        GroupMember member = new GroupMember();
        member.setGroup(savedGroup);
        member.setUser(creator);
        member.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(member);

        return savedGroup;
    }

    @Transactional
    public GroupMember joinGroupByInviteCode(String inviteCode, BaseUser user) {
        BaseGroup group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Convite inválido ou expirado."));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new UserAlreadyInGroupException("Usuário já pertence a este grupo.");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroup(group);
        newMember.setUser(user);
        newMember.setRole(GroupRole.MEMBER); // Novos membros entram com permissão base

        return groupMemberRepository.save(newMember);
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
        } while (groupRepository.existsByInviteCode(code)); // Valida no banco se o código já existe
        
        return code;
    }
}