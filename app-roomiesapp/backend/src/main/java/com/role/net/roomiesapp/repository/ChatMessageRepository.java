package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByGroupInviteCodeOrderByCreatedAtDesc(String inviteCode, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.id = :id")
    Optional<ChatMessage> findByIdWithSender(@Param("id") Long id);
    
}