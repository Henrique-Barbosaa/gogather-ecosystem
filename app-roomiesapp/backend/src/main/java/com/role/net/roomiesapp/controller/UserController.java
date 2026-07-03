package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.pix.PixInfoResponse;
import com.role.net.roomiesapp.dto.pix.RegisterPixKeyRequest;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.UserPixService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserPixService userPixService;

    public UserController(UserPixService userPixService) {
        this.userPixService = userPixService;
    }

    @GetMapping("/pix")
    public ResponseEntity<PixInfoResponse> getPixInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userPixService.getPixInfo(user));
    }

    @PatchMapping("/pix")
    public ResponseEntity<PixInfoResponse> registerOrUpdatePixKey(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterPixKeyRequest request) {
        return ResponseEntity.ok(userPixService.registerOrUpdatePix(user, request));
    }

    @PutMapping("/pix")
    public ResponseEntity<PixInfoResponse> updatePixKey(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterPixKeyRequest request) {
        return ResponseEntity.ok(userPixService.registerOrUpdatePix(user, request));
    }
}
