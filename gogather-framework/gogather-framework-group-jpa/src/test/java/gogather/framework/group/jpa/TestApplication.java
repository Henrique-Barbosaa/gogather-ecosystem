package gogather.framework.group.jpa;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.domain.BaseUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import jakarta.persistence.Entity;

@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
// Instância Final de Grupo (Cartucho do Jogo)
@Entity
class MockGroup extends BaseGroup {
}

// Instância Final de Usuário (Cartucho do Jogo)
@Entity
class MockUser extends BaseUser {
}

// Ponto Flexível (Hot Spot) implementado para o Teste
// Sem isso, o Orquestrador não liga. Aqui dizemos: "Para o teste, todo convite é válido!"
@Component
class DummyValidationStrategy implements GroupInviteValidationStrategy {
    @Override
    public void validate(Group group, Participant inviter, Participant invitee) {
        // Nenhuma exceção lançada = Convite Válido!
    }
}

@org.springframework.stereotype.Repository
interface MockGroupRepository extends gogather.framework.group.jpa.repository.BaseGroupRepository<MockGroup> {
}