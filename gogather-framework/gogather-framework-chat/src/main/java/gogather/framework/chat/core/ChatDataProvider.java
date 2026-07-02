package gogather.framework.chat.core;

import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;

public interface ChatDataProvider {
    boolean canAccessRoom(String roomId, String senderId);
    ChatMessageDTO persistMessage(SendMessageCommand command);
}
