package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.auth.TokenResponse;
import com.role.net.tripmaker.entity.RefreshToken;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.RefreshTokenRepository;
import gogather.framework.security.orchestrator.SecurityOrchestrator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("appTokenService")
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityOrchestrator securityOrchestrator;

    public TokenService(RefreshTokenRepository refreshTokenRepository, SecurityOrchestrator securityOrchestrator) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityOrchestrator = securityOrchestrator;
    }

    public String generateAccessToken(User user) {
        return securityOrchestrator.generateAccessToken(user.getUsername());
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
            .secure(false) // Alterar para true em produção (HTTPS)
            .path("/")
            .maxAge(2 * 60 * 60)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String token) {
        return ResponseCookie
            .from("refreshToken", token)
            .httpOnly(true)
            .secure(false) // Alterar para true em produção (HTTPS)
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie generateCleanCookie(String name, String path) {
        return ResponseCookie
            .from(name, "")
            .httpOnly(true)
            .secure(false)
            .path(path)
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public TokenResponse updateTokens(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token inválido ou não encontrado!"));

        if (refreshToken.getExpireDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ResourceNotFoundException("Refresh token expirado. Por favor, faça login novamente.");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush();

        String newRefreshToken = this.generateRefreshToken(user);
        String newAccessToken = this.generateAccessToken(user);

        return new TokenResponse(newRefreshToken, newAccessToken);
    }
}
