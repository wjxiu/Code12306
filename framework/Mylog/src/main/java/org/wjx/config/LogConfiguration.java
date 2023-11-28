package org.wjx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.wjx.LogHandler;
import org.springframework.boot.SpringBootConfiguration;

/**
 * @author xiu
 * @create 2023-11-20 10:31
 */
@Configuration
public class LogConfiguration {
    @ConditionalOnMissingBean
    public LogHandler log(){
        return new LogHandler();
    }

}
