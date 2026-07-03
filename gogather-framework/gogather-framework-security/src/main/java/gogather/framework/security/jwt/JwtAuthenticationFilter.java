package gogather.framework.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import gogather.framework.security.orchestrator.SecurityOrchestrator;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityOrchestrator securityOrchestrator;

    public JwtAuthenticationFilter(SecurityOrchestrator securityOrchestrator) {
        this.securityOrchestrator = securityOrchestrator;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            Optional<Authentication> authenticationOpt = securityOrchestrator.authenticateToken(token);
            authenticationOpt.ifPresent(authentication ->
                SecurityContextHolder.getContext().setAuthentication(authentication)
            );
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
