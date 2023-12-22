package org.wjx;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xiu
 * @create 2023-12-21 13:06
 */
@Component
public class TestController implements BeanNameAware, InitializingBean, BeanPostProcessor, DisposableBean {
    @Override
    public void setBeanName(String s) {
        System.out.println("setBeanName   "+s);
    }
    @PostConstruct
    public void init() {
        System.out.println("init");
        // ...
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("postProcessBeforeInitialization");
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("postProcessAfterInitialization");
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("destroy");
    }
}