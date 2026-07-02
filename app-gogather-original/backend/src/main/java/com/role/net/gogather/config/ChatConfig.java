package com.role.net.gogather.config;

import com.role.net.gogather.config.interceptor.AiMentionInterceptor;
import gogather.framework.chat.core.ChatDataProvider;
import gogather.framework.chat.orchestrator.ChatOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatConfig {

    @Bean
    public ChatOrchestrator chatOrchestrator(ChatDataProvider dataProvider, AiMentionInterceptor aiMentionInterceptor) {
        // Registra o provider e os plugins/interceptors na inicialização
        return new ChatOrchestrator(dataProvider, List.of(aiMentionInterceptor));
    }
}
