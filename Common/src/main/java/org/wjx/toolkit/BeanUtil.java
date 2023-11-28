package org.wjx.toolkit;

import org.dozer.DozerBeanMapper;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;

/**
 * @author xiu
 * @create 2023-11-20 18:01
 */
public class BeanUtil {
    static DozerBeanMapper mapper =new DozerBeanMapper();
    public static <T,S> T convert(S source,T target){
        Optional.of(source).ifPresent((S s) ->{mapper.map(s,target);});
        return target;
    }

    public static  <T,S> T convert(S source,Class<T> targetclazz){
        return Optional.ofNullable(source).map((S s) -> {
            return mapper.map(s, targetclazz);
        }).orElse(null);
    }
    public static  <T,S> List<T> convertToList(List<S> source,Class<T> targetclazz){
        if (source==null)return null;
        return source.stream().map(s -> {
            return mapper.map(s, targetclazz);
        }).toList();
    }
    public static  <T,S> T[] ListconvertToArray(List<S> source,Class<T> targetclazz){
        T[] targets =(T[]) Array.newInstance(targetclazz);
        if (source==null|| source.isEmpty())return targets;
        for (int i = 0; i < targets.length; i++) {
            targets[i]=mapper.map(source.get(i), targetclazz);
        }
       return targets;
    }

}
