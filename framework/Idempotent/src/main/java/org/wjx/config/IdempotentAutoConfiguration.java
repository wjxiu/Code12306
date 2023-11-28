package org.wjx.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wjx.core.*;
import org.wjx.param.IdempotentParamExeciteHandler;
import org.wjx.param.IdempotentParamService;
import org.wjx.spel.IdempotentSPELExecuteHandler;
import org.wjx.spel.IdempotentSPELService;
import org.wjx.token.IdempotentTokenController;
import org.wjx.token.IdempotentTokenExecuteHandler;
import org.wjx.token.IdempotentTokenService;

/**
 * @author xiu
 * @create 2023-11-27 14:03
 */

//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({IdempotentProperties.class})
public class IdempotentAutoConfiguration {
    /**
     * 幂等切面,主要工作在幂等切面完成
     * @return
     */
    @Bean
    public IdempotentAspect idempotentAspect(){
        return new IdempotentAspect();
    }

    /**
     * 幂等参数处理器
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentParamService idempotentParamExeciteHandler(RedissonClient redissonClient){
        return new IdempotentParamExeciteHandler(redissonClient);
    }
    @Bean
    @ConditionalOnMissingBean
    public IdempotentTokenController idempotentTokenController(IdempotentTokenExecuteHandler handler) {
        return new IdempotentTokenController(handler);
    }

    /**
     * 幂等spel表达式处理器
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentSPELService idempotentSPELService(RedissonClient redissonClient){
       return  new IdempotentSPELExecuteHandler( redissonClient);
    }

    /**
     * 幂等token处理器
     * @param safeCache
     * @param idempotentProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentTokenService idempotentTokenService(SafeCache safeCache,IdempotentProperties idempotentProperties){
        return new IdempotentTokenExecuteHandler(safeCache,idempotentProperties);
    }
//    todo 幂等mq处理器
}
