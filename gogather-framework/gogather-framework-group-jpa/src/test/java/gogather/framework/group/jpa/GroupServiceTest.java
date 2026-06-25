package gogather.framework.group.jpa;

import gogather.framework.group.jpa.domain.GroupMember;
import gogather.framework.group.jpa.domain.GroupRole;
import gogather.framework.group.jpa.repository.GroupMemberRepository;
import gogather.framework.group.jpa.service.GroupService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@Transactional // Dá rollback no banco após o teste (mantém tudo limpo)
public class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void deveCriarGrupoGerarInviteCodeESetarAdmin() {
        MockUser criador = new MockUser();
        criador.setName("Flawbert");
        criador.setEmail("flawbert@teste.com");
        entityManager.persist(criador); // Salva o usuário no banco em memória

        //cria o grupo cru
        MockGroup novoGrupo = new MockGroup();
        novoGrupo.setName("Rolê do Fim de Semana");
        novoGrupo.setDescription("Teste do framework");

        MockGroup grupoSalvo = groupService.createGroup(novoGrupo, criador);

        assertNotNull(grupoSalvo.getId(), "O ID do grupo não deveria ser nulo");
        
        assertNotNull(grupoSalvo.getInviteCode());
        assertEquals(8, grupoSalvo.getInviteCode().length(), "O código deve ter 8 caracteres");
        assertTrue(grupoSalvo.getInviteCode().matches("[A-Z0-9]+"), "O código deve ser alfanumérico em maiúsculo");

        Optional<GroupMember> associacao = groupMemberRepository.findByGroupIdAndUserId(grupoSalvo.getId(), criador.getId());
        assertTrue(associacao.isPresent(), "Deveria existir um registro na tabela group_member");
        assertEquals(GroupRole.ADMIN, associacao.get().getRole(), "O criador tem que ser ADMIN absoluto");

        System.out.println("SUCESSO! Código gerado: " + grupoSalvo.getInviteCode());
    }
}
