package org.wjx.core;

/**
 * 对应缓存为空时的执行操作
 * @author xiu
 * @create 2023-11-24 20:10
 */
@FunctionalInterface
public interface CacheGetIfAbsent<T> {
    /**
     * 缓存为空的操作
     * @param param
     */
    void process(T param);
}
