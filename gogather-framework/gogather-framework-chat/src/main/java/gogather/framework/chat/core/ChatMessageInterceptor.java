package gogather.framework.chat.core;

import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import java.util.Map;

public interface ChatMessageInterceptor {
    void preProcess(SendMessageCommand command);
    void postProcess(ChatMessageDTO savedMessage, Map<String, Object> metadata);
}
