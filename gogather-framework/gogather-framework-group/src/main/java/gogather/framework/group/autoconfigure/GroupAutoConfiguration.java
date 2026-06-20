package gogather.framework.group.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gogather.framework.group.core.GroupDataProvider;
import gogather.framework.group.core.GroupInviteValidationStrategy;
import gogather.framework.group.frozen.GroupMembershipOrchestrator;

@Configuration
public class GroupAutoConfiguration {

    @Bean
    @ConditionalOnBean({GroupDataProvider.class, GroupInviteValidationStrategy.class})
    @ConditionalOnMissingBean
    public GroupMembershipOrchestrator groupMembershipOrchestrator(
            GroupDataProvider dataProvider,
            GroupInviteValidationStrategy validationStrategy) {
        return new GroupMembershipOrchestrator(dataProvider, validationStrategy);
    }
}
