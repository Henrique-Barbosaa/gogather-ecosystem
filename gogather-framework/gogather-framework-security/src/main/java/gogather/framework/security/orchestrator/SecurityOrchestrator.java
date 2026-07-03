package gogather.framework.security.orchestrator;

import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import gogather.framework.security.core.SecurityDataProvider;
import gogather.framework.security.jwt.TokenService;

/**
 * Ponto Congelado (Frozen Spot) do framework de segurança.
 * Assume o controle total sobre o fluxo de autenticação e validação de tokens JWT,
 * invocando o gancho (Hot Spot) fornecido pela aplicação consumidora (SecurityDataProvider)
 * respeitando a Regra de Hollywood ("Don't call us, we'll call you").
 */
public class SecurityOrchestrator {

    private final TokenService tokenService;
    private final SecurityDataProvider dataProvider;

    public SecurityOrchestrator(TokenService tokenService, SecurityDataProvider dataProvider) {
        this.tokenService = tokenService;
        this.dataProvider = dataProvider;
    }

    /**
     * Valida um token de acesso, extrai a identificação do usuário e invoca a camada
     * de dados da aplicação para certificar se a conta existe e está habilitada.
     *
     * @param token String bruta do token JWT.
     * @return Optional contendo o objeto Authentication pronto para o SecurityContext, se válido.
     */
    public Optional<Authentication> authenticateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<String> subjectOpt = tokenService.validateTokenAndGetSubject(token);
        if (subjectOpt.isEmpty()) {
            return Optional.empty();
        }

        String username = subjectOpt.get();
        Optional<UserDetails> userDetailsOpt = dataProvider.loadUserByUsername(username);

        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
            if (dataProvider.isUserAuthorized(userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                return Optional.of(authentication);
            }
        }

        return Optional.empty();
    }

    /**
     * Gera um novo token de acesso delegado ao TokenService.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return tokenService.generateToken(userDetails);
    }

    /**
     * Gera um novo token de acesso baseado apenas no subject (identificador principal).
     */
    public String generateAccessToken(String subject) {
        return tokenService.generateToken(subject);
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    public SecurityDataProvider getDataProvider() {
        return dataProvider;
    }
}
