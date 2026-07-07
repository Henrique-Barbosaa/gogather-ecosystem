package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByGroupInviteCodeOrderByCreatedAtDesc(String inviteCode, Pageable pageable);
    List<ChatMessage> findByGroupIdOrderByCreatedAtAsc(Long groupId);
}
