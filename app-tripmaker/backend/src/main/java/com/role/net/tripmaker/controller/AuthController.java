package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.auth.ChangePasswordRequest;
import com.role.net.tripmaker.dto.auth.LoginRequest;
import com.role.net.tripmaker.dto.auth.RefreshRequest;
import com.role.net.tripmaker.dto.auth.RegisterUserRequest;
import com.role.net.tripmaker.dto.auth.RegisterUserResponse;
import com.role.net.tripmaker.dto.auth.TokenResponse;
import com.role.net.tripmaker.dto.user.UserResponse;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.InvalidCredentialsException;
import com.role.net.tripmaker.service.AuthService;
import com.role.net.tripmaker.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(
        AuthenticationManager authenticationManager,
        AuthService authService,
        TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        User newUser = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new RegisterUserResponse(
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getDisplayName()
            )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authReq);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Usuário ou senha inválidos!");
        }

        User user = (User) authentication.getPrincipal();

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        ResponseCookie jwtCookie = tokenService.generateAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = tokenService.generateRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(new TokenResponse(refreshToken, accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
        @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
        @RequestBody(required = false) RefreshRequest body
    ) {
        String token = refreshTokenCookie != null ? refreshTokenCookie : (body != null ? body.refreshToken() : null);
        if (token != null) {
            tokenService.revokeRefreshToken(token);
        }

        ResponseCookie deleteAccess = tokenService.generateCleanCookie("accessToken", "/");
        ResponseCookie deleteRefresh = tokenService.generateCleanCookie("refreshToken", "/");

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
            .body("Logout realizado com sucesso!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
        @RequestBody(required = false) RefreshRequest body
    ) {
        String token = refreshTokenCookie != null ? refreshTokenCookie : (body != null ? body.refreshToken() : null);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token não fornecido no cookie ou no corpo da requisição.");
        }

        TokenResponse tokenResponse = tokenService.updateTokens(token);

        ResponseCookie jwtCookie = tokenService.generateAccessTokenCookie(tokenResponse.accessToken());
        ResponseCookie refreshCookie = tokenService.generateRefreshTokenCookie(tokenResponse.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(tokenResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado!");
        }
        authService.changePassword(user, request);
        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/verify")
    public ResponseEntity<UserResponse> verify(@AuthenticationPrincipal User user) {
        return me(user);
    }
}
