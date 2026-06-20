package gogather.framework.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class TokenService {

    private final JwtProperties jwtProperties;

    public TokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
            return JWT.create()
                    .withIssuer("gogather-framework")
                    .withSubject(userDetails.getUsername())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public Optional<String> validateTokenAndGetSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
            String subject = JWT.require(algorithm)
                    .withIssuer("gogather-framework")
                    .build()
                    .verify(token)
                    .getSubject();
            return Optional.ofNullable(subject);
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}
