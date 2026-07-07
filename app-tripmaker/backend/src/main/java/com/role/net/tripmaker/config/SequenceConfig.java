package com.role.net.tripmaker.config;

import gogather.framework.sequence.SequenceService;
import gogather.framework.sequence.strategy.LinearSequenceStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SequenceConfig {

    @Bean
    public SequenceService sequenceService() {
        return new SequenceService(new LinearSequenceStrategy<>());
    }
}
