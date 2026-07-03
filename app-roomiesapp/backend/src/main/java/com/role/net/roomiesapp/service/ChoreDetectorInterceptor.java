package com.role.net.roomiesapp.service;

import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.core.ChatMessageInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChoreDetectorInterceptor implements ChatMessageInterceptor {

    @Override
    public void preProcess(SendMessageCommand command) {
        String content = command.content();
        if (content != null && (content.toLowerCase().startsWith("/tarefa ") || content.toLowerCase().startsWith("/chore "))) {
            String[] parts = content.split(" ", 2);
            if (parts.length >= 2) {
                command.metadata().put("isChore", true);
                command.metadata().put("choreDescription", parts[1]);
            }
        }
    }

    @Override
    public void postProcess(ChatMessageDTO savedMessage, Map<String, Object> metadata) {
        if (metadata != null && Boolean.TRUE.equals(metadata.get("isChore"))) {
            System.out.println("====== PLUGIN DE TAREFAS DISPARADO ======");
            System.out.println("Nova tarefa criada para a casa: " + metadata.get("choreDescription"));
            System.out.println("Por usuário: " + savedMessage.senderId());
            System.out.println("=========================================");
        }
    }
}
