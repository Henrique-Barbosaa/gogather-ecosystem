package gogather.framework.group.jpa;

import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.domain.BaseUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.persistence.Entity;

@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}

// Grupo Concreto só para o teste
@Entity
class MockGroup extends BaseGroup {
    //ele herda tudo do BaseGroup!
}

// Usuário Concreto só para o teste
@Entity
class MockUser extends BaseUser {
    //herda de BaseUser
}
