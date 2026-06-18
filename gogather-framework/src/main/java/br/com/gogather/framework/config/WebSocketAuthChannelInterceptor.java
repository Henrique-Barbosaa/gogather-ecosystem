package br.com.gogather.framework.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String USER_ATTR = "AUTHENTICATED_USER";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            var sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null) {
                Object auth = sessionAttributes.get(USER_ATTR);

                if (auth instanceof UsernamePasswordAuthenticationToken token) {
                    accessor.setUser(token);
                }
            }
        }

        return message;
    }
}
