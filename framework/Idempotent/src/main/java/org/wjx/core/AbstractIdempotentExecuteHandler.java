package org.wjx.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.wjx.annotation.Idempotent;

/**
 * @author xiu
 * @create 2023-11-23 10:40
 */
@Slf4j
public abstract class AbstractIdempotentExecuteHandler implements IdempotentExecuteHandler{
    public abstract IdempotentParamWrapper build(ProceedingJoinPoint joinPoint);
    /**
     * 保证幂等性的具体逻辑
     * @param wrapper 包装类
     */
    public abstract void  handle( IdempotentParamWrapper wrapper);
    public void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent){
        IdempotentParamWrapper wrapper = build(joinPoint);
        wrapper.idempotent=idempotent;
        log.info("包装类:{}---------------",wrapper);
        handle(wrapper);
    }
}
