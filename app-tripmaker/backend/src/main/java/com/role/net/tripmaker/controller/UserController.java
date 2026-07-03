package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.user.RegisterPixKeyRequest;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/pix")
    public ResponseEntity<Void> registerPix(
        @RequestBody @Valid RegisterPixKeyRequest request,
        @AuthenticationPrincipal User user
    ) {
        userService.registerPix(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
