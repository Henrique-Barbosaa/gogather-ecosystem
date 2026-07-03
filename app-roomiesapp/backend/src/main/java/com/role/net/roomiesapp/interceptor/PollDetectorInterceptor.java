package com.role.net.roomiesapp.interceptor;

import com.role.net.roomiesapp.dto.chat.PollRequest;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.PollService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class PollDetectorInterceptor implements ChannelInterceptor {

    private final PollService pollService;

    public PollDetectorInterceptor(PollService pollService) {
        this.pollService = pollService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null && accessor.getCommand().name().equals("SEND")) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/app/chat/")) {
                String inviteCode = destination.substring("/app/chat/".length());

                if (message.getPayload() instanceof byte[] payloadBytes) {
                    String payloadStr = new String(payloadBytes, StandardCharsets.UTF_8);

                    if (payloadStr.startsWith("\"/poll ")) {
                        String commandContent = payloadStr.substring(7, payloadStr.length() - 1).trim(); // Remove "/poll " and closing quote
                        
                        // Parse command: Question? Option1, Option2, Option3
                        int questionMarkIndex = commandContent.indexOf('?');
                        if (questionMarkIndex != -1) {
                            String question = commandContent.substring(0, questionMarkIndex + 1).trim();
                            String optionsStr = commandContent.substring(questionMarkIndex + 1).trim();
                            
                            List<String> options = Arrays.stream(optionsStr.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList();
                            
                            if (options.size() >= 2) {
                                UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) accessor.getUser();
                                if (principal != null && principal.getPrincipal() instanceof User user) {
                                    PollRequest request = new PollRequest(question, options);
                                    pollService.createPoll(inviteCode, request, user);
                                    return null; // Intercepta e não processa a mensagem de texto normal
                                }
                            }
                        }
                    }
                }
            }
        }
        return message;
    }
}
