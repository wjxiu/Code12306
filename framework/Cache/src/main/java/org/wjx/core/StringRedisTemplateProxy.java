package org.wjx.core;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.wjx.Exception.ServiceException;
import org.wjx.config.RedisCustomProperties;
import org.wjx.utils.Cacheutil;
import org.wjx.utils.FastJson2Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author xiu
 * @create 2023-11-25 14:36
 */
//@Component
@RequiredArgsConstructor
public class StringRedisTemplateProxy implements SafeCache {
    final StringRedisTemplate redisTemplate;
    final RedisCustomProperties redisproperties;
    final RedissonClient redissonClient;
    final String KEYPREFIX = "REDIS:LOCK:PREFIX:";


    @Override
    public <T> T get(String key, Class<T> clazz) {
        String s = redisTemplate.opsForValue().get(key);
        if (String.class.isAssignableFrom(clazz)) {
            return (T) s;
        }
        return JSON.parseObject(s, FastJson2Util.buildType(clazz));
    }


    @Override
    public void put(String key, Object value) {
        put(key, value, redisproperties.timeOut, redisproperties.timeUnit);
    }

    public <HK, HV> void putOfHash(String key, HK hashkey, HV value) {
        HashOperations<String, HK, HV> hashOperations = getInstance().opsForHash();
        hashOperations.put(key, hashkey, value);
    }

    public <HK, HV> HV SafeGetOfHash(String key, HK hashkey, CacheLoader<HV> loader) {
        HashOperations<String, HK, HV> hashOperations = getInstance().opsForHash();
        HV hv = hashOperations.get(key, hashkey);
        if (hv != null) return hv;
        RLock lock = redissonClient.getLock(KEYPREFIX + key + ":" + hashkey);
        boolean b = lock.tryLock();
        try {
            if (b) {
                hv = hashOperations.get(key, hashkey);
                if (hv != null) return hv;
                hv = loader.load();
                hashOperations.put(key, hashkey, hv);
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        if (hv != null) return hv;
        else throw new ServiceException("加载缓存失败");
    }

    public void put(String key, Object value, Long timeout) {
        put(key, value, timeout, redisproperties.timeUnit);
    }

    public void put(String key, Object value, Long timeout, TimeUnit timeUnit) {
        if (value instanceof String) {
            redisTemplate.opsForValue().set(key, (String) value, timeout, timeUnit);
        } else
            redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, timeUnit);
    }

//    @Override
//    public Boolean putIfAllAbsent(Collection<String> keys) {
//        Resource scriptSource = new ClassPathResource("scripts/putIfAllAbsent.lua");
//        RedisScript<Boolean> booleanRedisScript = RedisScript.of(scriptSource, Boolean.class);
//        redisTemplate.execute(booleanRedisScript,keys);
//        return null;
//    }

    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public RedisTemplate getInstance() {
        return redisTemplate;
    }

    @Override
    public <T> T get(String key, Class<T> returnType, Long timeout, CacheLoader<T> loader) {
        return get(key, returnType, timeout, redisproperties.timeUnit, loader);
    }

    @Override
    public <T> T get(String key, Class<T> returnType, Long timeout, TimeUnit timeUnit, CacheLoader<T> loader) {
        T value = get(key, returnType);
        if (Cacheutil.isNuLLOrBlank(value)) {
            return loadAndPut(key, timeout, timeUnit, loader, false, null);
        }
        return value;
    }


    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout,
                         CacheLoader<T> cacheLoader,
                         RBloomFilter<String> bloomFilter,
                         CacheGetFilter<String> cacheCheckFilter,
                         CacheGetIfAbsent<String> cacheGetIfAbsent) {
        return safeGet(key, clazz, timeout, redisproperties.timeUnit, cacheLoader, bloomFilter, cacheCheckFilter, cacheGetIfAbsent);
    }


    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, CacheLoader<T> cacheLoader, RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, timeout, redisproperties.timeUnit, cacheLoader, bloomFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, TimeUnit timeUnit,
                         CacheLoader<T> cacheLoader, RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, timeout, timeUnit, cacheLoader, bloomFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, CacheLoader<T> cacheLoader) {
        return safeGet(key, clazz, timeout, redisproperties.timeUnit, cacheLoader, null, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, TimeUnit timeUnit, CacheLoader<T> cacheLoader) {
        return safeGet(key, clazz, timeout, timeUnit, cacheLoader, null, null);
    }

    public <T> List<T> safeGetForList(String key, Class<T> clazz, long timeout, TimeUnit timeUnit, CacheLoader<List<T>> cacheLoader) {
        ListOperations<String, T> listOperations = getInstance().opsForList();
        List<T> range = listOperations.range(key, 0, -1);
        if (range != null && !range.isEmpty()) return range;
        RLock lock = redissonClient.getLock(KEYPREFIX + key);
        boolean b = lock.tryLock();
        try {
            if (b) {
                range = listOperations.range(key, 0, -1);
                if (range != null && !range.isEmpty()) return range;
                range = cacheLoader.load();
                Long l = listOperations.leftPushAll(key, range);
                getInstance().expire(key, timeout, timeUnit);
                if (l != range.size()) throw new ServiceException("缓存列表出错");
            }
        } finally {
            lock.unlock();
        }

        return range;
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, TimeUnit timeUnit,
                         CacheLoader<T> cacheLoader,
                         RBloomFilter<String> bloomFilter,
                         CacheGetFilter<String> cacheGetFilter) {
        return safeGet(key, clazz, timeout, timeUnit, cacheLoader, bloomFilter, cacheGetFilter, null);
    }


    @Override
    public void put(String key, Object value, long timeout) {
        put(key, value, timeout, redisproperties.timeUnit);
    }

    @Override
    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (value instanceof String) {
            redisTemplate.opsForValue().set(key, value.toString(), timeout, timeUnit);
        } else {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, timeUnit);
        }
    }

    @Override
    public void safePut(String key, Object value, long timeout, RBloomFilter<String> bloomFilter) {
        put(key, value, timeout);
        if (bloomFilter != null) bloomFilter.add(key);
    }

    @Override
    public void safePut(String key, Object value, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter) {
        put(key, value, timeout, timeUnit);
        if (bloomFilter != null) bloomFilter.add(key);
    }

    /**
     * 判断keys是否都存在
     *
     * @return 都存在true, 否则false
     */
    @Override
    public Long countExistingKeys(String... keys) {
        return redisTemplate.countExistingKeys(Arrays.asList(keys));
    }


    /**
     * 通过 布隆过滤器 cacheCheckFilter(布隆过滤器白名单) cacheGetIfAbsent 缓存为空的逻辑
     * 并且使用KEYPREFIX+key作为 key获取redis中的value
     *
     * @param key
     * @param clazz            返回值的类型
     * @param timeout          过期时间,默认30s
     * @param timeUnit         时间单位,默认毫秒
     * @param cacheLoader      如果缓存不存在,定义加载缓存的逻辑
     * @param bloomFilter      自定义布隆过滤器
     * @param cacheCheckFilter 过滤缓存结果
     * @param cacheGetIfAbsent 缓存查询为空的执行逻辑
     * @param <T>
     * @return
     */
    @Override
    public <T> T safeGet(String key, Class<T> clazz, long timeout, TimeUnit timeUnit,
                         CacheLoader<T> cacheLoader,
                         RBloomFilter<String> bloomFilter,
                         CacheGetFilter<String> cacheCheckFilter,
                         CacheGetIfAbsent<String> cacheGetIfAbsent) {
        T value = get(key, clazz);
//        结果不为空 或者 不在布隆过滤器 或者在布隆过滤器白名单(filter,用于补充布隆过滤器不能删除的缺点)里边的
        if (!Cacheutil.isNuLLOrBlank(value)
                || Optional.ofNullable(cacheCheckFilter).map(each -> each.filter(key)).orElse(false)
                || Optional.ofNullable(bloomFilter).map(each -> !each.contains(key)).orElse(false)
        ) {
            return value;
        }
        RLock lock = redissonClient.getLock(KEYPREFIX + key);
        try {
            if (lock.tryLock()) {
                if (!Cacheutil.isNuLLOrBlank(value = get(key, clazz))) {
                    return value;
                }
                value = loadAndPut(key, timeout, timeUnit, cacheLoader, true, bloomFilter);
                if (Cacheutil.isNuLLOrBlank(value)) {
                    Optional.ofNullable(cacheGetIfAbsent).ifPresent(each -> each.process(key));
                }
            }
        } finally {
            lock.unlock();
        }
        return value;
    }

    /**
     * 调用loader接口执行 返回需要缓存的值
     * 并且根据safeflag决定是否使用布隆过滤器
     *
     * @param key         缓存的key
     * @param timeout     过期时间,默认30s
     * @param timeUnit    时间单位,默认毫秒
     * @param loader      将 loader返回的值作为value保存到缓存中
     * @param savetobloom true 生成缓存并且保存到RBloomFilter;false 不保存到RBloomFilter,只生成缓存
     * @param bloomFilter 被添加的RBloomFilter
     * @param <T>         返回值的类型
     * @return 缓存后的值
     */
    private <T> T loadAndPut(String key, Long timeout, TimeUnit timeUnit, CacheLoader<T> loader, Boolean savetobloom, RBloomFilter<String> bloomFilter) {
        T value = loader.load();
        if (Cacheutil.isNuLLOrBlank(value)) return value;
        if (savetobloom) {
            safePut(key, value, timeout, timeUnit, bloomFilter);
        } else {
            put(key, value, timeout, timeUnit);
        }
        return value;
    }
}
