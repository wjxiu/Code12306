package org.wjx.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.wjx.Exception.ClientException;
import org.wjx.annotation.Idempotent;

import java.lang.reflect.Method;

/**
 * @author xiu
 * @create 2023-11-24 18:32
 */
@Aspect@Slf4j
public class IdempotentAspect {
    @Around("@annotation(org.wjx.annotation.Idempotent)")
    public Object idempotentHandler(ProceedingJoinPoint joinPoint) {
        Object res = null;
        IdempotentExecuteHandler handler = null;
        log.info("开始前的结果---------{}",res);
        try {
            Idempotent idempotent = getIdempotent(joinPoint);
            handler = IdempotentExecuteHandlerFactory.getBean(idempotent.scene(), idempotent.type());
            handler.execute(joinPoint, idempotent);
            res = joinPoint.proceed();
            log.info("proceed后的结果---------{}",res);
            handler.postExecute();
        } catch (Throwable e) {
            log.info("抛出异常---------{}",res);
            assert handler != null;
            handler.exceptionProcess();
            throw new ClientException(e.getMessage());
        } finally {
            IdempotentContext.clear();
        }
        log.info("返回之前的结果---------{}",res);
        return res;
    }

    public static Idempotent getIdempotent(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent annotation = method.getAnnotation(Idempotent.class);
        return annotation;
    }
}
