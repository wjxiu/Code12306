package org.wjx.core;

/**
 * @author xiu
 * @create 2023-11-25 15:02
 */
@FunctionalInterface
public interface CacheLoader<T>{
    /**
     * 加载缓存
     * @return 缓存结果
     */
    T load();
}
