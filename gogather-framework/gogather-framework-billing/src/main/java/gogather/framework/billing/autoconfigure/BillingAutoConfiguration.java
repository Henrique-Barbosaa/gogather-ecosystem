package gogather.framework.billing.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gogather.framework.billing.core.AbstractExpenseSplitter;
import gogather.framework.billing.core.BillingDataProvider;
import gogather.framework.billing.orchestrator.BillingOrchestrator;
import gogather.framework.billing.strategies.SimpleEqualSplitStrategy;

@Configuration
public class BillingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AbstractExpenseSplitter defaultExpenseSplitter() {
        return new SimpleEqualSplitStrategy();
    }

    @Bean
    @ConditionalOnBean(BillingDataProvider.class)
    @ConditionalOnMissingBean
    public BillingOrchestrator billingOrchestrator(
            BillingDataProvider dataProvider,
            AbstractExpenseSplitter expenseSplitter) {
        return new BillingOrchestrator(dataProvider, expenseSplitter);
    }
}
