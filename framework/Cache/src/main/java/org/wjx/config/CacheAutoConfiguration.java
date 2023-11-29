package org.wjx.config;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.wjx.RedisKeySerializer;
import org.wjx.core.StringRedisTemplateProxy;


/**
 * @author xiu
 * @create 2023-11-26 20:41
 */
//@Configuration
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@RequiredArgsConstructor
public class CacheAutoConfiguration {

    private final RedisCustomProperties redisDistributedProperties;
    @Bean
    public RedisKeySerializer redisKeySerializer() {
        String prefix = RedisCustomProperties.PREFIX;
        String prefixCharset = redisDistributedProperties.getPrefixCharset();
        return new RedisKeySerializer(prefix, prefixCharset);
    }
    @Bean
    public StringRedisTemplateProxy proxy(RedisKeySerializer redisKeySerializer,
                                          StringRedisTemplate template,
                                          RedissonClient redissonClient){
        template.setKeySerializer(redisKeySerializer);
        return  new StringRedisTemplateProxy(template, redisDistributedProperties, redissonClient);
    }
    @Bean
    @ConditionalOnProperty(prefix = BloomFilterProperties.PREFIX,name="enable",havingValue = "true")
    public RBloomFilter<String> rBloomFilter(RedissonClient redissonClient, BloomFilterProperties bloomFilterProperties){
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomFilterProperties.getName());
        bloomFilter.tryInit(bloomFilterProperties.getExpectedInsertions(),bloomFilterProperties.getFalseProbability());
        return bloomFilter;
    }
}
