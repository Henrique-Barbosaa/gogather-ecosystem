package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.chat.PollRequest;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.PollService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups/{inviteCode}/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping
    public ResponseEntity<Void> createPoll(
            @PathVariable String inviteCode,
            @RequestBody PollRequest request,
            @AuthenticationPrincipal User user
    ) {
        pollService.createPoll(inviteCode, request, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/options/{optionId}/vote")
    public ResponseEntity<Void> vote(
            @PathVariable String inviteCode,
            @PathVariable Long optionId,
            @AuthenticationPrincipal User user
    ) {
        pollService.vote(optionId, inviteCode, user);
        return ResponseEntity.ok().build();
    }
}
