package org.wjx.user.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author xiu
 * @create 2023-11-24 19:18
 */
public class ApplicationContextHolder implements ApplicationContextAware {
   static ApplicationContext context;

    @Override
    public  void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    public static  <T>T  getBean(Class<T> tClass){
        return context.getBean(tClass);
    }
    public static Object  getBean(String name){
      return  context.getBean(name);
    }
    public static  <T> T getBean(String name,Class<T> tClass){
        return context.getBean(name,tClass);
    }
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return context.findAnnotationOnBean(beanName, annotationType);
    }
    public static ApplicationContext getContext(){
        return context;
    }
}
