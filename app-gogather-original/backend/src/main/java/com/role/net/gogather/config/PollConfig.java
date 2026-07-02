package com.role.net.gogather.config;

import gogather.framework.polling.core.PollingDataProvider;
import gogather.framework.polling.orchestrator.PollingOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollConfig {

    @Bean
    public PollingOrchestrator pollingOrchestrator(PollingDataProvider dataProvider) {
        return new PollingOrchestrator(dataProvider);
    }
}
