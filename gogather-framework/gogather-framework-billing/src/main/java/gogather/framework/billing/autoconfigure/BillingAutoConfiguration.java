package gogather.framework.billing.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gogather.framework.billing.core.ExpenseSplitStrategy;
import gogather.framework.billing.strategies.SimpleEqualSplitStrategy;

@Configuration
public class BillingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExpenseSplitStrategy defaultSplitStrategy() {
        return new SimpleEqualSplitStrategy();
    }

}
