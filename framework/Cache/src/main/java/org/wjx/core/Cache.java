package org.wjx.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.redis.core.RedisTemplate;


import java.util.Collection;

/**
 * @author xiu
 * @create 2023-11-24 20:07
 */
public interface Cache {
    /**
     * 获取缓存
     */
    <T> T get(@NotBlank String key, Class<T> clazz);

    /**
     * 放入缓存
     */
    void put(@NotBlank String key, Object value);
    public <HK, HV> HV SafeGetOfHash(String key, HK hashkey, CacheLoader<HV> loader);
        /**
         * 如果 keys 全部不存在，则新增，返回 true，反之 false
         */
//    Boolean putIfAllAbsent(@NotNull Collection<String> keys);

    /**
     * 删除缓存
     */
    Boolean delete(@NotBlank String key);

    /**
     * 删除 keys，返回删除数量
     */
    Long delete(@NotNull Collection<String> keys);

    /**
     * 判断 key 是否存在
     */
    Boolean hasKey(@NotBlank String key);

    /**
     * 获取缓存组件实例
     */
    RedisTemplate getInstance();
}
