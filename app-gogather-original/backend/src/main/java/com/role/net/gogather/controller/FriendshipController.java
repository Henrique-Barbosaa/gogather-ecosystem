package com.role.net.gogather.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.role.net.gogather.dto.friendship.FriendshipResponse;
import com.role.net.gogather.dto.friendship.FriendshipSimpleResponse;
import com.role.net.gogather.entity.Friendship;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.service.FriendshipService;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public ResponseEntity<List<FriendshipSimpleResponse>> getFriends(
        @AuthenticationPrincipal User user
    ) {
        List<FriendshipSimpleResponse> friends = friendshipService
            .friends(user.getId());
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipSimpleResponse>> getPending(
        @AuthenticationPrincipal User user
    ) {
        List<Friendship> pending = friendshipService.pending(user.getId());
        if(pending.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(
            pending.stream()
                .map(obj -> FriendshipSimpleResponse.from(obj.getRequester(), obj))
                .toList()
        );
    }

    @GetMapping("/find/{externalId}")
    public ResponseEntity<FriendshipResponse> getFriendship(
        @AuthenticationPrincipal User user,
        @PathVariable UUID externalId
    ) {
        Friendship friendship = friendshipService.friendship(user.getExternalId(), externalId);

        if(friendship == null) return ResponseEntity.noContent().build();

        return ResponseEntity.ok(
          FriendshipResponse.from(friendship)
        );
    }

    @GetMapping("/{fsId}")
    public ResponseEntity<FriendshipResponse> requestDetails(
        @AuthenticationPrincipal User user,
        @PathVariable UUID fsId
    ) {
        FriendshipResponse response = friendshipService
            .details(user.getId(), fsId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/{recId}")
    public ResponseEntity<FriendshipResponse> sendRequest(
        @AuthenticationPrincipal User user,
        @PathVariable UUID recId
    ) {
        Friendship sent = friendshipService
            .send(user.getId(), recId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FriendshipResponse.from(sent)
            );
    }

    @PatchMapping("/accept/{fsId}")
	public ResponseEntity<FriendshipResponse> acceptRequest(
	    @AuthenticationPrincipal User user,
		@PathVariable UUID fsId
	) {
        FriendshipResponse response = friendshipService
            .accept(user.getId(), fsId);
        return ResponseEntity.ok(response);
	}

	@PatchMapping("/refuse/{fsId}")
	public ResponseEntity<FriendshipResponse> refuseRequest(
	    @AuthenticationPrincipal User user,
		@PathVariable UUID fsId
	) {
        FriendshipResponse response = friendshipService
            .refuse(user.getId(), fsId);
        return ResponseEntity.ok(response);
	}

	@PatchMapping("/unfriend/{fsId}")
	public ResponseEntity<FriendshipResponse> unfriendRequest(
	    @AuthenticationPrincipal User user,
		@PathVariable UUID fsId
	) {
        FriendshipResponse response = friendshipService
            .unfriend(user.getId(), fsId);
        return ResponseEntity.ok(response);
	}
}
