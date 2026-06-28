package com.role.net.gogather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gogather.framework.sequence.SequenceService;
import gogather.framework.sequence.strategy.LinearSequenceStrategy;

@Configuration
public class SequenceConfig {

    @Bean
    public SequenceService sequenceService() {
        return new SequenceService(new LinearSequenceStrategy<>());
    }
}
