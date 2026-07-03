package com.role.net.roomiesapp.config;

import gogather.framework.chat.orchestrator.ChatOrchestrator;
import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.core.ChatDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RoomiesChatConfig {

    @Bean
    public ChatOrchestrator chatOrchestrator(ChatDataProvider chatDataProvider, List<ChatMessageInterceptor> interceptors) {
        return new ChatOrchestrator(chatDataProvider, interceptors);
    }
}
