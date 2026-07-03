package com.role.net.tripmaker.config;

import gogather.framework.chat.core.ChatDataProvider;
import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.orchestrator.ChatOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TripmakerChatConfig {

    @Bean
    public ChatOrchestrator chatOrchestrator(ChatDataProvider chatDataProvider, List<ChatMessageInterceptor> interceptors) {
        return new ChatOrchestrator(chatDataProvider, interceptors);
    }
}
