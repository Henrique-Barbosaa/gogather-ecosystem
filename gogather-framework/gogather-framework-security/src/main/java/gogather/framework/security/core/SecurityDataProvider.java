package gogather.framework.security.core;

import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Hot Spot do módulo de segurança (Princípio de Hollywood: "Don't call us, we'll call you").
 * A aplicação consumidora implementa esta interface para fornecer
 * acesso aos dados de usuários e credenciais a partir de sua própria camada de persistência
 * ou provedor de identidade.
 */
public interface SecurityDataProvider {

    /**
     * Carrega os detalhes do usuário a partir do identificador principal (e-mail, username, ID, etc.).
     *
     * @param username identificador principal do usuário.
     * @return Optional contendo os dados do usuário, ou empty se não encontrado.
     */
    Optional<UserDetails> loadUserByUsername(String username);

    /**
     * Gancho opcional para verificar se a conta do usuário está ativa/habilitada para autenticação.
     * O comportamento padrão considera o usuário ativo caso o objeto UserDetails retorne os status positivos.
     *
     * @param userDetails objeto retornado por loadUserByUsername.
     * @return true se o usuário está autorizado a se autenticar, false caso contrário.
     */
    default boolean isUserAuthorized(UserDetails userDetails) {
        return userDetails != null 
                && userDetails.isEnabled() 
                && userDetails.isAccountNonExpired() 
                && userDetails.isAccountNonLocked() 
                && userDetails.isCredentialsNonExpired();
    }
}
