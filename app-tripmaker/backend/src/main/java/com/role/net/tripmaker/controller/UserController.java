package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.friend.FriendRequestDto;
import com.role.net.tripmaker.dto.friend.FriendResponse;
import com.role.net.tripmaker.dto.user.RegisterPixKeyRequest;
import com.role.net.tripmaker.dto.user.UpdateProfileRequest;
import com.role.net.tripmaker.dto.user.UserResponse;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.FriendshipService;
import com.role.net.tripmaker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendshipService friendshipService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
        @RequestBody UpdateProfileRequest request,
        @AuthenticationPrincipal User user
    ) {
        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @PatchMapping("/pix")
    public ResponseEntity<Void> registerPix(
        @RequestBody @Valid RegisterPixKeyRequest request,
        @AuthenticationPrincipal User user
    ) {
        userService.registerPix(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendshipService.getUserFriends(user));
    }

    @PostMapping("/friends/request")
    public ResponseEntity<FriendResponse> sendFriendRequest(
        @RequestBody @Valid FriendRequestDto request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(friendshipService.sendFriendRequest(user, request));
    }

    @PostMapping("/friends/{id}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        friendshipService.acceptFriendRequest(user, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/friends/{id}")
    public ResponseEntity<Void> removeFriendship(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        friendshipService.removeOrDeclineFriendship(user, id);
        return ResponseEntity.ok().build();
    }
}

