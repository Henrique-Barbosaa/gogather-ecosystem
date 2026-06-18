package com.role.net.gogather.controller;

import com.role.net.gogather.dto.auth.LoginRequest;
import com.role.net.gogather.dto.auth.RegisterUserRequest;
import com.role.net.gogather.dto.auth.RegisterUserResponse;
import com.role.net.gogather.dto.auth.TokenResponse;
import com.role.net.gogather.dto.user.UserResponse;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.AuthService;
import com.role.net.gogather.service.TokenService;

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

    @PostMapping("/login")
    public ResponseEntity<String> login(
        @Valid @RequestBody LoginRequest request
    ) {

        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authentication = authenticationManager.authenticate(userAndPass);
        User user = (User) authentication.getPrincipal();

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        ResponseCookie jwtCookie = tokenService.generateAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = tokenService.generateRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body("Login efetuado com sucesso!");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        User newUser = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new RegisterUserResponse(
                newUser.getUsername(),
                newUser.getEmail())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if(refreshToken != null){
            tokenService.revokeRefreshToken(refreshToken);
        }

        ResponseCookie deleteAccess = tokenService.generateCleanCookie("accessToken", "/");
        ResponseCookie deleteRefresh = tokenService.generateCleanCookie("refreshToken", "/auth/refresh");

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
            .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = "refreshToken") String refreshToken) {
        TokenResponse tokenResponse = tokenService.updateTokens(refreshToken);

        ResponseCookie jwtCookie = tokenService.generateAccessTokenCookie(tokenResponse.accessToken());
        ResponseCookie refreshCookie = tokenService.generateRefreshTokenCookie(tokenResponse.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body("Refresh feito!");
    }

    @GetMapping("/verify")
    public ResponseEntity<UserResponse> verify(
        @AuthenticationPrincipal User user
    ) {
		if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
            UserResponse.from(user)
        );
    }
}
