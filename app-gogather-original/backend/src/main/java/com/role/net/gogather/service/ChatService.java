package com.role.net.gogather.service;

import com.role.net.gogather.entity.ChatMessage;
import com.role.net.gogather.entity.Group;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.enums.GroupMemberStatus;
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

import java.util.UUID;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ChatMessage saveMessage(Long groupId, Long userId, ChatMessageRequest request) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found."));

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!groupRepository.isGroupMember(groupId, userId, GroupMemberStatus.ACTIVE)) {
            throw new UserNotAGroupMemberException("You are not a member of this group.");
        }

		ChatMessage message = ChatMessage.builder()
				.group(group)
				.sender(sender)
				.content(request.content())
				.type(MessageType.USER)
				.build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

		// o frontend envia um boolean dizendo se a mensagem requer resposta da IA
		// depois vemos se é a melhor forma, mas acho que por enquanto está bom.
        if (request.requiresAiResponse()) {
            eventPublisher.publishEvent(new AiMentionEvent(groupId, request.content()));
        }

        return savedMessage;
    }

	@Transactional(readOnly = true)
	public Page<ChatMessageResponse> getMessagesByGroup(UUID externalId, Long userId, Pageable pageable) {
		if (!groupRepository.findByExternalId(externalId).isPresent()) {
			throw new ResourceNotFoundException("Group not found.");
		}

		if (!groupRepository.isGroupMemberByExternalId(externalId, userId, GroupMemberStatus.ACTIVE)) {
			throw new UserNotAGroupMemberException("You are not a member of this group.");
		}

		return chatMessageRepository.findByGroupExternalIdOrderByCreatedAtDesc(externalId, pageable)
				.map(ChatMessageResponse::from);
	}
}
