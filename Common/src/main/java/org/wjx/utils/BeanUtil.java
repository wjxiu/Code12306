package org.wjx.utils;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.loader.api.BeanMappingBuilder;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;

import static com.github.dozermapper.core.loader.api.TypeMappingOptions.mapEmptyString;
import static com.github.dozermapper.core.loader.api.TypeMappingOptions.mapNull;

/**
 * @author xiu
 * @create 2023-11-20 18:01
 */
public class BeanUtil {
    static Mapper mapper= DozerBeanMapperBuilder.buildDefault();
    public static <T,S> T convert(S source,T target){
        Optional.of(source).ifPresent((S s) ->{mapper.map(s,target);});
        return target;
    }
    public static  <T,S> T convert(S source,Class<T> targetclazz){
        return Optional.ofNullable(source).map((S s) -> {
            return mapper.map(s, targetclazz);
        }).orElse(null);
    }
    /**
     * 拷贝非空且非空串属性
     *
     * @param source 数据源
     * @param target 指向源
     */
    public static void convertIgnoreNullAndBlank(Object source, Object target) {
        DozerBeanMapperBuilder dozerBeanMapperBuilder = DozerBeanMapperBuilder.create();
        Mapper mapper = dozerBeanMapperBuilder.withMappingBuilders(new BeanMappingBuilder() {

            @Override
            protected void configure() {
                mapping(source.getClass(), target.getClass(), mapNull(false), mapEmptyString(false));
            }
        }).build();
        mapper.map(source, target);
    }
    public static  <T,S> List<T> convertToList(List<S> source,Class<T> targetclazz){
        if (source==null)return null;
        return source.stream().map(s -> mapper.map(s, targetclazz)).toList();
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
