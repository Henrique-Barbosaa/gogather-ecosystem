package gogather.framework.chat.orchestrator;

import gogather.framework.chat.core.ChatDataProvider;
import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;

import java.util.List;

public class ChatOrchestrator {
    private final ChatDataProvider dataProvider;
    private final List<ChatMessageInterceptor> interceptors;

    public ChatOrchestrator(ChatDataProvider dataProvider, List<ChatMessageInterceptor> interceptors) {
        this.dataProvider = dataProvider;
        this.interceptors = interceptors;
    }

    public ChatMessageDTO processMessage(SendMessageCommand command) {
        if (!dataProvider.canAccessRoom(command.roomId(), command.senderId())) {
            throw new SecurityException("Acesso negado à sala de chat.");
        }

        if (interceptors != null) {
            interceptors.forEach(i -> i.preProcess(command));
        }

        ChatMessageDTO saved = dataProvider.persistMessage(command);

        if (interceptors != null) {
            interceptors.forEach(i -> i.postProcess(saved, command.metadata()));
        }

        return saved;
    }
}
