package com.role.net.gogather.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.role.net.gogather.dto.user.RegisterPixKeyRequest;
import com.role.net.gogather.dto.user.UpdateUserRequest;
import com.role.net.gogather.dto.user.UpdateUserResponse;
import com.role.net.gogather.dto.user.UserSearchResponse;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{search}")
    public ResponseEntity<List<UserSearchResponse>> getUserBySearch(
        @AuthenticationPrincipal User user,
        @PathVariable String search
    ) {
        List<User> users = userService.findBySearch(search);
        return ResponseEntity
            .ok(users.stream()
                .filter(obj -> !obj.getId().equals(user.getId()))
                .map(UserSearchResponse::from)
                .toList()
            );
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<UpdateUserResponse> updateUser(
        @PathVariable String id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = this.userService.update(
            id,
            request.username(),
            request.displayName(),
            request.email(),
            request.password(),
            request.newPassword(),
            request.birthDate()
        );

        return ResponseEntity.ok(UpdateUserResponse.from(user));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable String id
    ) {
        this.userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/pix")
    public ResponseEntity<Void> registerPix(
        @AuthenticationPrincipal User user,
        @RequestBody RegisterPixKeyRequest request
    ) {
        userService.registerPix(user.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
