package gogather.framework.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenService tokenService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("my-test-secret");
        jwtProperties.setExpirationMinutes(60L);
        tokenService = new TokenService(jwtProperties);
    }

    @Test
    void testGenerateAndValidateTokenWithSubject() {
        String subject = "test-user-id-123";
        String token = tokenService.generateToken(subject);

        assertNotNull(token);
        Optional<String> validatedSubject = tokenService.validateTokenAndGetSubject(token);
        assertTrue(validatedSubject.isPresent());
        assertEquals(subject, validatedSubject.get());
    }

    @Test
    void testGenerateAndValidateTokenWithUserDetails() {
        UserDetails userDetails = new User("alice@example.com", "password", Collections.emptyList());
        String token = tokenService.generateToken(userDetails);

        assertNotNull(token);
        Optional<String> validatedSubject = tokenService.validateTokenAndGetSubject(token);
        assertTrue(validatedSubject.isPresent());
        assertEquals("alice@example.com", validatedSubject.get());
    }

    @Test
    void testInvalidTokenReturnsEmpty() {
        Optional<String> validatedSubject = tokenService.validateTokenAndGetSubject("invalid.jwt.token");
        assertTrue(validatedSubject.isEmpty());
    }
}
