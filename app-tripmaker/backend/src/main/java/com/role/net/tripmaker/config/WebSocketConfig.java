package com.role.net.tripmaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;
    private final com.role.net.tripmaker.interceptor.PollDetectorInterceptor pollDetectorInterceptor;

    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor,
            WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor,
            com.role.net.tripmaker.interceptor.PollDetectorInterceptor pollDetectorInterceptor
    ) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.webSocketAuthChannelInterceptor = webSocketAuthChannelInterceptor;
        this.pollDetectorInterceptor = pollDetectorInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor, pollDetectorInterceptor);
    }
}
