package org.wjx.core;


import org.redisson.api.RBloomFilter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xiu
 * @create 2023-11-25 14:56
 */
public interface SafeCache extends Cache{
    /**
     * 获取缓存,如果查询为空,通过CacheIfAbsent接口定义获取
     * @param key 键
     * @param timeout key的过期时间
     * @param returnType 返回类型的Class
     * @param loader 如果缓存为空的操作
     * @return 结果
     * @param <T> 返回类型的泛型
     */
    <T> T get(String key,Class<T> returnType, Long timeout,  CacheLoader<T> loader);
    /**
     * 获取缓存,如果查询为空,通过CacheIfAbsent接口定义获取
     * @param key 键
     * @param timeout key的过期时间
     * @param timeUnit 过期时间单位
     * @param returnType 返回类型的Class
     * @param loader 如果缓存为空的操作
     * @return 结果
     * @param <T> 返回类型的泛型
     */
    <T> T get(String key,Class<T> returnType, Long timeout, TimeUnit timeUnit,  CacheLoader<T> loader);

    /**
     * 通过 布隆过滤器 cacheCheckFilter(布隆过滤器白名单) cacheGetIfAbsent 缓存为空的逻辑
     * 并且使用KEYPREFIX+key作为 key获取redis中的value
     * @param key
     * @param clazz 返回值的类型
     * @param timeout 过期时间,默认30s
     * @param timeUnit 时间单位,默认毫秒
     * @param cacheLoader 如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter 自定义布隆过滤器
     * @param cacheCheckFilter 过滤缓存结果
     * @param cacheGetIfAbsent 缓存查询为空的执行逻辑
     * @return
     * @param <T>
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz,  long timeout, TimeUnit timeUnit,
                  CacheLoader<T> cacheLoader, RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter, CacheGetIfAbsent<String> cacheGetIfAbsent);
    /**
     *通过安全的方式获取缓存
     * @param cacheLoader 如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter 自定义布隆过滤器
     * @param cacheCheckFilter 过滤缓存结果
     * @param cacheGetIfAbsent 缓存查询为空的执行逻辑
     * @return 缓存的结果
     * @param <T> 缓存的类型
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz,  long timeout,CacheLoader<T> cacheLoader,
                  RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter, CacheGetIfAbsent<String> cacheGetIfAbsent);
    /**
     *通过安全的方式获取缓存
     * @param cacheLoader 如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter 自定义布隆过滤器
     * @return 缓存的结果
     * @param <T> 缓存的类型
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz,  long timeout, TimeUnit timeUnit, CacheLoader<T> cacheLoader, RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheGetFilter);
    /**
     *通过安全的方式获取缓存
     * @param cacheLoader 如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter 自定义布隆过滤器
     * @return 缓存的结果
     * @param <T> 缓存的类型
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz, long timeout, CacheLoader<T> cacheLoader,  RBloomFilter<String> bloomFilter);
    /**
     *通过安全的方式获取缓存
     * @param cacheLoader 如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter 自定义布隆过滤器
     * @return 缓存的结果
     * @param <T> 缓存的类型
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz,long timeout,TimeUnit timeUnit, CacheLoader<T> cacheLoader,  RBloomFilter<String> bloomFilter);

    <T> T safeGet(@NotBlank String key, Class<T> clazz,long timeout, CacheLoader<T> cacheLoader);

    /**
     *
     */
    <T> T safeGet(@NotBlank String key, Class<T> clazz,long timeout, TimeUnit timeUnit, CacheLoader<T> cacheLoader);


    /**
     * 生成针对list的缓存
     * @param key
     * @param clazz
     * @param timeout
     * @param timeUnit
     * @param cacheLoader
     * @return
     * @param <T>
     */
    public  <T> List<T> safeGetForList(String key, Class<T> clazz, long timeout, TimeUnit timeUnit, CacheLoader<List<T>> cacheLoader);
    /**
     * 放入缓存，自定义超时时间
     */
    void put(@NotBlank String key, Object value, long timeout);

    /**
     * 这里的value会自动转为json,如果传入的是字符串或者json不会被序列化为json,而是直接存入
     * 放入缓存，自定义超时时间
     */
    void put(@NotBlank String key, Object value, long timeout, TimeUnit timeUnit);

    void safePut(@NotBlank String key, Object value, long timeout, RBloomFilter<String> bloomFilter);
    void safePut(@NotBlank String key, Object value, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter);
    Long countExistingKeys(@NotNull String... keys);


}
