package com.role.net.roomiesapp.service;

import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import gogather.framework.chat.core.ChatMessageInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.UserRepository;

@Component
public class ChoreDetectorInterceptor implements ChatMessageInterceptor {

    private final ChoreService choreService;
    private final RoomiesGroupRepository groupRepository;
    private final UserRepository userRepository;

    public ChoreDetectorInterceptor(ChoreService choreService, RoomiesGroupRepository groupRepository, UserRepository userRepository) {
        this.choreService = choreService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

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
            try {
                Group group = groupRepository.findByInviteCode(savedMessage.roomId()).orElseThrow();
                User creator = userRepository.findById(Long.parseLong(savedMessage.senderId())).orElseThrow();
                String choreText = (String) metadata.get("choreDescription");

                // Tarefas criadas via chat não têm descrição/data separadas: usamos o texto
                // do comando como título e deixamos os outros campos em branco.
                choreService.createChore(group, creator, choreText, null, null);

                System.out.println("====== PLUGIN DE TAREFAS DISPARADO ======");
                System.out.println("Nova tarefa criada para a casa: " + choreText);
                System.out.println("Por usuário: " + creator.getUsername());
                System.out.println("=========================================");
            } catch (Exception e) {
                System.err.println("Erro ao criar tarefa via chat: " + e.getMessage());
            }
        }
    }
}
