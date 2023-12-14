package org.wjx.config;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.wjx.RedisKeyValueSerializer;
import org.wjx.core.StringRedisTemplateProxy;


/**
 * @author xiu
 * @create 2023-11-26 20:41
 */
@RequiredArgsConstructor
public class CacheAutoConfiguration {

    private final RedisCustomProperties redisDistributedProperties;
    @Bean
    @ConditionalOnMissingBean
    public RedisKeyValueSerializer redisKeySerializer() {
        String prefix = RedisCustomProperties.PREFIX;
        String prefixCharset = redisDistributedProperties.getPrefixCharset();
        return new RedisKeyValueSerializer(prefix, prefixCharset);
    }
    @Bean
    public StringRedisTemplateProxy proxy(RedisKeyValueSerializer redisKeyValueSerializer,
                                          StringRedisTemplate template,
                                          RedissonClient redissonClient){
        template.setKeySerializer(redisKeyValueSerializer);
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashKeySerializer(genericJackson2JsonRedisSerializer);
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);
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
