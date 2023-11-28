package org.wjx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author xiu
 * @create 2023-11-27 13:53
 */
@Data
@ConfigurationProperties(prefix =IdempotentProperties.PREFIX )
public class IdempotentProperties {
    public static final String PREFIX = "frame.idempotent.token";
    private String keyPrefix;

    /**
     * Token 申请后过期时间
     * 单位默认毫秒 {@link TimeUnit#MILLISECONDS}
     * 随着分布式缓存过期时间单位 {@link RedisCustomProperties#timeOut} 而变化
     */
    private Long timeout;
}
