package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.notice.CreateNoticeRequest;
import com.role.net.roomiesapp.dto.notice.NoticeResponse;
import com.role.net.roomiesapp.entity.HouseNotice;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households/{groupId}/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateNoticeRequest request,
            @AuthenticationPrincipal User loggedUser) {
        HouseNotice saved = noticeService.createNotice(groupId, request, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(NoticeResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getNotices(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        List<NoticeResponse> responses = noticeService.getNotices(groupId, loggedUser).stream()
                .map(NoticeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long groupId,
            @PathVariable Long noticeId,
            @AuthenticationPrincipal User loggedUser) {
        noticeService.deleteNotice(groupId, noticeId, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
