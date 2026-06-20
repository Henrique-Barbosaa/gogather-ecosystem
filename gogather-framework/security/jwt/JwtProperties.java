package br.com.gogather.framework.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "framework.security.jwt")
public class JwtProperties {

    private String secret = "default-secret-key-change-in-production";

    private Long expirationMinutes = 120L;

}
