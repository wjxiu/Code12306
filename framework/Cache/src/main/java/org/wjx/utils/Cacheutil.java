package org.wjx.utils;

import org.springframework.util.StringUtils;

import java.util.stream.Stream;

/**
 * @author xiu
 * @create 2023-11-24 19:56
 */
public class Cacheutil {
    public static String buildKey(String... keys){
        for (String key:keys) {
            if (!StringUtils.hasLength(key)) throw new RuntimeException("缓存key为空");
        }
        return  String.join("::", keys);
    }
    /**
     *兼容泛型的值是否为空或者null
     * 如果是字符串,额外判断字符串是否为空串
     * @return 如果为空或者null 返回true,否则返回false
     */
    public static boolean isNuLLOrBlank(Object cacheVal){
        if (cacheVal==null)return true;
        if (cacheVal instanceof String){
            return ((String)cacheVal).isEmpty();
        }
        return false;
    }
}
