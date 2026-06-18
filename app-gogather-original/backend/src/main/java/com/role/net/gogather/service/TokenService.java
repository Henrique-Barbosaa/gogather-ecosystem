package com.role.net.gogather.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.role.net.gogather.config.JWTUserData;
import com.role.net.gogather.dto.auth.TokenResponse;
import com.role.net.gogather.entity.RefreshToken;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.repository.RefreshTokenRepository;


@Service
public class TokenService {

    @Value("${spring.api.security.token.secret}")
	private String secret;

	private final RefreshTokenRepository refreshTokenRepository;

	public TokenService(RefreshTokenRepository refreshTokenRepository) {
	    this.refreshTokenRepository = refreshTokenRepository;
	}

	public String generateAccessToken(User user) {

        Algorithm algorithm = Algorithm.HMAC256(secret);

	    return JWT.create()
				.withClaim("userId", user.getId())
				.withSubject(user.getUsername())
				.withExpiresAt(Instant.now().plusSeconds(1200))
				.withIssuedAt(Instant.now())
				.sign(algorithm);
	}

	@Transactional
	public String generateRefreshToken(User user) {
		refreshTokenRepository.deleteByUserId(user.getId());
		refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpireDate(Instant.now().plus(7, ChronoUnit.DAYS));

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
	}

	public ResponseCookie generateAccessTokenCookie(String token) {
        return ResponseCookie
            .from("accessToken", token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(20*60)
            .sameSite("Strict")
            .build();
	}

	public ResponseCookie generateRefreshTokenCookie(String token) {
	    return ResponseCookie
			.from("refreshToken", token)
            .httpOnly(true)
            .secure(true)
            .path("/auth/refresh")
            .maxAge(7*24*60*60)
            .sameSite("Strict")
            .build();
	}

    public ResponseCookie generateCleanCookie(String name, String path) {
        return ResponseCookie
            .from(name, "")
            .httpOnly(true)
            .secure(true)
            .path(path)
            .maxAge(0)
            .sameSite("Strict")
            .build();
    }

	public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token " + token + " não existe!"));
        refreshTokenRepository.delete(refreshToken);
	}

	public Optional<JWTUserData> validateToken(String token) {

        try {

            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decode = JWT.require(algorithm).build().verify(token);
            JWTUserData jwtUserData = new JWTUserData(decode.getClaim("userId").asLong(), decode.getSubject());
            return Optional.of(jwtUserData);

        } catch(JWTVerificationException ex){
            return Optional.empty();
        }
	}

	public TokenResponse updateTokens(String rtoken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rtoken)
            .orElseThrow(() -> new RuntimeException("Unknown refresh token."));

        if(refreshToken.getExpireDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        String newRefreshToken = this.generateRefreshToken(user);
        String newAccessToken = this.generateAccessToken(user);

        return new TokenResponse(newRefreshToken, newAccessToken);
	}

}
