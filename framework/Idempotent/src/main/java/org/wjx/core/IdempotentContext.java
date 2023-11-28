package org.wjx.core;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wjx.Exception.ServiceException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 保存redission的lock,用于解锁
 * @author xiu
 * @create 2023-11-22 21:10
 */
public class IdempotentContext {
    static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);
    public static Map<String,Object> getMap(){
        return  CONTEXT.get();
    }
    public static Object get(String key){
        if (StringUtils.hasLength(key)){
           return  getMap().get(key);
        }
        return null;
    }
    public static void put(String key,Object obj){
        if (!StringUtils.hasLength(key))throw new ServiceException("幂等接口的key为空");
        if (Objects.isNull(obj))throw new ServiceException("幂等接口的value为空");
        Map<String, Object> map = getMap();
        if (map==null){
            map=new HashMap<>();
            map.put(key,obj);
            CONTEXT.set(map);
        }
        map.put(key,obj);
    }
    public static void putContext(Map<String, Object> context) {
        if (Objects.isNull(context)||context.isEmpty())throw new ServiceException("context为null");
        Map<String, Object> map = getMap();
        map.putAll(context);
        CONTEXT.set(map);
    }
    public static void clear(){
        CONTEXT.remove();
    }

}
