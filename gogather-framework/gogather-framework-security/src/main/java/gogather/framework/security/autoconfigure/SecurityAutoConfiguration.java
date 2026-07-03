package gogather.framework.security.autoconfigure;

import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gogather.framework.security.core.SecurityDataProvider;
import gogather.framework.security.jwt.JwtAuthenticationFilter;
import gogather.framework.security.jwt.JwtProperties;
import gogather.framework.security.jwt.TokenService;
import gogather.framework.security.orchestrator.SecurityOrchestrator;

@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenService tokenService(JwtProperties jwtProperties) {
        return new TokenService(jwtProperties);
    }

    @Bean
    @ConditionalOnBean(UserDetailsService.class)
    @ConditionalOnMissingBean(SecurityDataProvider.class)
    public SecurityDataProvider defaultUserDetailsServiceAdapter(UserDetailsService userDetailsService) {
        return new SecurityDataProvider() {
            @Override
            public Optional<UserDetails> loadUserByUsername(String username) {
                try {
                    return Optional.ofNullable(userDetailsService.loadUserByUsername(username));
                } catch (Exception ex) {
                    return Optional.empty();
                }
            }
        };
    }

    @Bean
    @ConditionalOnBean(SecurityDataProvider.class)
    @ConditionalOnMissingBean
    public SecurityOrchestrator securityOrchestrator(TokenService tokenService, SecurityDataProvider securityDataProvider) {
        return new SecurityOrchestrator(tokenService, securityDataProvider);
    }

    @Bean
    @ConditionalOnBean(SecurityOrchestrator.class)
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(SecurityOrchestrator securityOrchestrator) {
        return new JwtAuthenticationFilter(securityOrchestrator);
    }

    @Bean
    @ConditionalOnBean(JwtAuthenticationFilter.class)
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(401, "Unauthorized");
                        })
                )
                .build();
    }
}
