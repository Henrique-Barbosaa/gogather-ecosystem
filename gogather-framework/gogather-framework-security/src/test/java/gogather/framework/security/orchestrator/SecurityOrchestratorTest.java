package gogather.framework.security.orchestrator;

import gogather.framework.security.core.SecurityDataProvider;
import gogather.framework.security.jwt.JwtProperties;
import gogather.framework.security.jwt.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityOrchestratorTest {

    private TokenService tokenService;
    private SecurityOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("orchestrator-test-secret");
        tokenService = new TokenService(jwtProperties);
    }

    @Test
    void testHollywoodPrincipleAuthenticationSuccess() {
        // Hot Spot implementado pela aplicação consumidora
        SecurityDataProvider appDataProvider = new SecurityDataProvider() {
            @Override
            public Optional<UserDetails> loadUserByUsername(String username) {
                if ("bob@gogather.com".equals(username)) {
                    UserDetails user = new User(username, "secret", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        };

        // Frozen Spot (Orquestrador do Framework) assume o controle
        orchestrator = new SecurityOrchestrator(tokenService, appDataProvider);

        String token = orchestrator.generateAccessToken("bob@gogather.com");
        Optional<Authentication> authOpt = orchestrator.authenticateToken(token);

        assertTrue(authOpt.isPresent(), "O orquestrador deve autenticar com sucesso chamando o gancho da aplicação");
        assertEquals("bob@gogather.com", authOpt.get().getName());
        assertTrue(authOpt.get().isAuthenticated());
    }

    @Test
    void testHollywoodPrincipleUserNotFoundOrUnauthorized() {
        SecurityDataProvider appDataProvider = username -> Optional.empty();
        orchestrator = new SecurityOrchestrator(tokenService, appDataProvider);

        String token = orchestrator.generateAccessToken("non-existent-user");
        Optional<Authentication> authOpt = orchestrator.authenticateToken(token);

        assertTrue(authOpt.isEmpty(), "Deve retornar vazio quando o DataProvider não encontra ou não autoriza o usuário");
    }

    @Test
    void testHollywoodPrincipleDisabledUser() {
        SecurityDataProvider appDataProvider = new SecurityDataProvider() {
            @Override
            public Optional<UserDetails> loadUserByUsername(String username) {
                // Usuário desabilitado (disabled = false)
                UserDetails disabledUser = new User(username, "secret", false, true, true, true, Collections.emptyList());
                return Optional.of(disabledUser);
            }
        };
        orchestrator = new SecurityOrchestrator(tokenService, appDataProvider);

        String token = orchestrator.generateAccessToken("disabled@gogather.com");
        Optional<Authentication> authOpt = orchestrator.authenticateToken(token);

        assertTrue(authOpt.isEmpty(), "Não deve autenticar usuário desabilitado");
    }
}
