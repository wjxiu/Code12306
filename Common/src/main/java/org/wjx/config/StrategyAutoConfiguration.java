package org.wjx.config;

import Strategy.AbstractExecuteStrategy;
import Strategy.AbstractStrategyChoose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.user.core.ApplicationContextHolder;

/**
 * @author xiu
 * @create 2023-12-06 20:30
 */
@Configuration
@Slf4j
public class StrategyAutoConfiguration {
    /**
     * 策略模式选择器
     */
    @Bean
    public AbstractStrategyChoose abstractStrategyChoose() {
        log.info("abstractStrategyChooseabstractStrategyChoose");
        return new AbstractStrategyChoose();
    }

    /**
     * 责任链上下文
     */
    @Bean
    public AbstractFilterChainsContext abstractChainContext(ApplicationContext applicationContext) {
        log.info("abstractChainContextabstractChainContext");
        return new AbstractFilterChainsContext(applicationContext);
    }
    @Bean
    public ApplicationContextHolder applicationContextHolder(){
        return new ApplicationContextHolder();
    }
    @Bean
    @ConditionalOnMissingBean
    public ApplicationContentPostProcessor congoApplicationContentPostProcessor(ApplicationContext applicationContext) {
        return new ApplicationContentPostProcessor(applicationContext);
    }

}
