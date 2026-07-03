package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.notice.CreateNoticeRequest;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.HouseNotice;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.NoticeRepository;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final RoomiesGroupRepository groupRepository;

    public NoticeService(NoticeRepository noticeRepository, RoomiesGroupRepository groupRepository) {
        this.noticeRepository = noticeRepository;
        this.groupRepository = groupRepository;
    }

    private Group validateMembership(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada."));
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }
        return group;
    }

    @Transactional
    public HouseNotice createNotice(Long groupId, CreateNoticeRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        HouseNotice notice = HouseNotice.builder()
                .group(group)
                .creator(loggedUser)
                .title(request.title())
                .content(request.content())
                .build();

        return noticeRepository.save(notice);
    }

    @Transactional(readOnly = true)
    public List<HouseNotice> getNotices(Long groupId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        return noticeRepository.findByGroupIdOrderByCreatedAtNoticeDesc(groupId);
    }

    @Transactional
    public void deleteNotice(Long groupId, Long noticeId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        HouseNotice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Aviso não encontrado."));
        
        if (!notice.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Aviso não pertence a esta casa.");
        }

        noticeRepository.delete(notice);
    }
}
