package com.role.net.gogather.service;

import com.role.net.gogather.entity.ChatMessage;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.MessageType;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.exception.UserNotAGroupMemberException;
import com.role.net.gogather.dto.chat.AiMentionEvent;
import com.role.net.gogather.dto.chat.ChatMessageRequest;
import com.role.net.gogather.dto.chat.ChatMessageResponse;
import com.role.net.gogather.repository.ChatMessageRepository;
import com.role.net.gogather.repository.GroupRepository;
import com.role.net.gogather.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gogather.framework.chat.orchestrator.ChatOrchestrator;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.dto.ChatMessageDTO;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatOrchestrator chatOrchestrator;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher,
            ChatOrchestrator chatOrchestrator
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.chatOrchestrator = chatOrchestrator;
    }

    @Transactional
    public ChatMessage saveMessage(Long groupId, Long userId, ChatMessageRequest request) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requiresAi", request.requiresAiResponse());

        SendMessageCommand command = new SendMessageCommand(
            groupId.toString(),
            userId.toString(),
            request.content(),
            metadata
        );

        ChatMessageDTO savedDto = chatOrchestrator.processMessage(command);

        return chatMessageRepository.findById(Long.parseLong(savedDto.messageId()))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found after save"));
    }

	@Transactional(readOnly = true)
	public Page<ChatMessageResponse> getMessagesByGroup(UUID externalId, Long userId, Pageable pageable) {
		if (!groupRepository.findByExternalId(externalId).isPresent()) {
			throw new ResourceNotFoundException("Group not found.");
		}

		if (!groupRepository.isGroupMemberByExternalId(externalId, userId)) {
			throw new UserNotAGroupMemberException("You are not a member of this group.");
		}

		return chatMessageRepository.findByGroupExternalIdOrderByCreatedAtDesc(externalId, pageable)
				.map(ChatMessageResponse::from);
	}
}
