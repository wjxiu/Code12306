package org.wjx.core;

/**
 * @author xiu
 * @create 2023-11-24 20:09
 */

/**
 * 可以当作bloom的白名单,解决了bloom filter无法删除元素的困难
 * @param <T>
 */
@FunctionalInterface
public interface CacheGetFilter<T> {
    /**
     * 符合条件的保留下来,不符合的返回false
     * @param param
     * @return
     */
    boolean filter(T param);
    
}
