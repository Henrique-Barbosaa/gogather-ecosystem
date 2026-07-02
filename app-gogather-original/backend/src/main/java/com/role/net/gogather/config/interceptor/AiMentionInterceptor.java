package com.role.net.gogather.config.interceptor;

import com.role.net.gogather.dto.chat.AiMentionEvent;
import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiMentionInterceptor implements ChatMessageInterceptor {

    private final ApplicationEventPublisher eventPublisher;

    public AiMentionInterceptor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void preProcess(SendMessageCommand command) {
        // Sem pré-processamento necessário para IA
    }

    @Override
    public void postProcess(ChatMessageDTO savedMessage, Map<String, Object> metadata) {
        if (metadata != null) {
            Boolean requiresAi = (Boolean) metadata.getOrDefault("requiresAi", false);
            if (Boolean.TRUE.equals(requiresAi)) {
                eventPublisher.publishEvent(new AiMentionEvent(
                    Long.parseLong(savedMessage.roomId()), 
                    savedMessage.content()
                ));
            }
        }
    }
}
