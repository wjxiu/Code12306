package org.wjx.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cache.decorators.TransactionalCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通过代理全部的controller来开启或管理全局回滚,方便测试
 * @author xiu
 * @create 2023-11-27 19:30
 */
//@ConditionalOnProperty(prefix = "test",name = "enabled",havingValue = "true")
@Component
@Order(0)
@Aspect@Slf4j
@RequiredArgsConstructor
@RestController
public class RollBackAspect {
    /**
     * 全局事务回滚变量
     */
    static boolean gorool=false;

    /**
     * 设置gorool变量设置全局回滚变量是否开启
     * @param goroll 全局回滚变量,true 设置全局回滚,false关闭全局回滚
     * @return true 设置全局回滚,false关闭全局回滚
     */
    @GetMapping("/setfortest/roll/{goroll}")
    public boolean setrollback(@PathVariable boolean goroll){
        gorool=goroll;
        return gorool;
    }

    /**
     * 获取全局变量的值
     * @return true 开启全局回滚,false关闭全局回滚
     */
    @GetMapping("/getfortest/roll")
    public boolean getrollback(){
        return gorool;
    }
    final TransactionTemplate transactionTemplate;
    @Around("execution(* org.wjx.controller..*(..)))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!gorool){
            return  joinPoint.proceed();
        }
        Object proceed = null;PlatformTransactionManager transactionManager=null;
        TransactionStatus status=null;
        log.info("-----------------回滚开始-----------------------------");
        try {
             transactionManager = transactionTemplate.getTransactionManager();
            TransactionDefinition def = new DefaultTransactionDefinition();
            assert transactionManager != null;
            status = transactionManager.getTransaction(def);
            proceed = joinPoint.proceed();
        } finally {
            if (gorool){
                assert transactionManager != null;
                assert status != null;
                transactionManager.rollback(status);
                log.info("-------------回滚结束---------------------");
            }
        }
        return proceed;
    }
}
