package gogather.framework.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "framework.security.jwt")
public class JwtProperties {

    private String secret = "default-secret-key-change-in-production";

    private Long expirationMinutes = 120L;

}
