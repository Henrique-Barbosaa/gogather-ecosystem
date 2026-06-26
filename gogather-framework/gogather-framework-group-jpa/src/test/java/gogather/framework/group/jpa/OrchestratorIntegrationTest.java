package gogather.framework.group.jpa;

import gogather.framework.group.jpa.domain.GroupRole;
import gogather.framework.group.jpa.repository.BaseUserRepository;
import gogather.framework.group.jpa.service.GroupService;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@Transactional // Limpa o banco H2 após o teste
public class OrchestratorIntegrationTest {

    @Autowired
    private GroupService groupService; // Da camada JPA

    @Autowired
    private BaseUserRepository userRepository; // Da camada JPA

    @Autowired
    private MockGroupRepository groupRepository; // Da camada JPAS

    @Autowired
    private GroupMembershipOrchestrator orchestrator; // Da camada CORE (caixa-preta)

    @Test
    public void deveConectarCoreEJPA_AdicionandoMembroEmCascata() {
        
        MockUser criador = new MockUser();
        criador.setName("Admin da República");
        userRepository.save(criador);

        MockUser convidado = new MockUser();
        convidado.setName("Novo Morador");
        userRepository.save(convidado);

        MockGroup republica = new MockGroup();
        republica.setName("República Devs");
        MockGroup grupoSalvo = groupService.createGroup(republica, criador);

        // Validamos que o criador entrou como ADMIN
        assertEquals(1, grupoSalvo.getMembers().size());
        assertEquals(GroupRole.ADMIN, grupoSalvo.getMembers().get(0).getRole());

        orchestrator.inviteUserToGroup(
                grupoSalvo.getInviteCode(),      // O Core busca o grupo pelo inviteCode
                convidado.getId().toString(),    // O Core busca o usuário pelo ID string
                null                             // Sem convidador (ex: entrou por link)
        );

        MockGroup grupoAtualizado = groupRepository.findById(grupoSalvo.getId()).orElseThrow();

        System.out.println("✅ Status: O grupo agora tem " + grupoAtualizado.getMembers().size() + " membros.");
        
        // Verificamos se o CascadeType.ALL funcionou
        assertEquals(2, grupoAtualizado.getMembers().size(), "Deveria ter 1 Admin e 1 Membro");
        assertTrue(grupoAtualizado.hasMember(convidado.getId().toString()), "O orquestrador deveria ter adicionado o novo morador");
    }
}